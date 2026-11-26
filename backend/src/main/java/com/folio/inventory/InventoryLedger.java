package com.folio.inventory;

import com.folio.auth.AuthenticatedUser;
import com.folio.common.web.PageResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The append-only stock ledger (rule R7). Every stock movement writes exactly one entry in the caller's transaction. */
@Service
public class InventoryLedger {

    private static final String SELECT = """
            SELECT t.id, t.book_id, b.title AS book_title, b.isbn13, t.type, t.quantity_change,
                   t.total_before, t.total_after, t.available_before, t.available_after,
                   t.reason, t.note, t.loan_id, t.performed_at, u.id AS actor_id, u.name AS actor_name
              FROM inventory_transactions t
              JOIN books b ON b.id = t.book_id
              JOIN users u ON u.id = t.performed_by
            """;

    private final InventoryTransactionRepository transactions;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    public InventoryLedger(InventoryTransactionRepository transactions, NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.transactions = transactions;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(long bookId, TransactionType type, StockChange change, String reason, String note, Long loanId,
                       AuthenticatedUser actor) {
        transactions.save(new InventoryTransaction(bookId, type, change, reason, note, loanId, actor.id(), Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public PageResponse<LedgerEntry> search(LedgerFilter filter, int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String where = where(filter, params);
        Long total = jdbc.queryForObject("SELECT count(*) FROM inventory_transactions t" + where, params, Long.class);
        params.addValue("limit", size).addValue("offset", (long) page * size);
        List<LedgerEntry> content = jdbc.query(SELECT + where + " ORDER BY t.performed_at DESC, t.id DESC LIMIT :limit OFFSET :offset",
                params, (rs, i) -> map(rs));
        long totalElements = total == null ? 0 : total;
        return new PageResponse<>(content, page, size, totalElements, (int) Math.ceil((double) totalElements / size));
    }

    @Transactional(readOnly = true)
    public void stream(LedgerFilter filter, Consumer<LedgerEntry> consumer) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        jdbc.query(SELECT + where(filter, params) + " ORDER BY t.performed_at DESC, t.id DESC", params,
                rs -> {
                    consumer.accept(map(rs));
                });
    }

    private static String where(LedgerFilter filter, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        if (filter.bookId() != null) {
            where.append(" AND t.book_id = :bookId");
            params.addValue("bookId", filter.bookId());
        }
        if (filter.type() != null) {
            where.append(" AND t.type = :type");
            params.addValue("type", filter.type().name());
        }
        if (filter.actorId() != null) {
            where.append(" AND t.performed_by = :actorId");
            params.addValue("actorId", filter.actorId());
        }
        if (filter.from() != null) {
            where.append(" AND t.performed_at >= :from");
            params.addValue("from", Timestamp.from(filter.from()));
        }
        if (filter.to() != null) {
            where.append(" AND t.performed_at < :to");
            params.addValue("to", Timestamp.from(filter.to()));
        }
        return where.toString();
    }

    private static LedgerEntry map(ResultSet rs) throws SQLException {
        return new LedgerEntry(
                rs.getLong("id"),
                new LedgerEntry.BookRef(rs.getLong("book_id"), rs.getString("book_title"), rs.getString("isbn13")),
                TransactionType.valueOf(rs.getString("type")),
                rs.getInt("quantity_change"),
                rs.getInt("total_before"),
                rs.getInt("total_after"),
                rs.getInt("available_before"),
                rs.getInt("available_after"),
                rs.getString("reason"),
                rs.getString("note"),
                rs.getObject("loan_id", Long.class),
                new LedgerEntry.ActorRef(rs.getLong("actor_id"), rs.getString("actor_name")),
                rs.getTimestamp("performed_at").toInstant());
    }

    public record LedgerFilter(Long bookId, TransactionType type, Long actorId, Instant from, Instant to) {
    }
}
