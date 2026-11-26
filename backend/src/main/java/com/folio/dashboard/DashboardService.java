package com.folio.dashboard;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    public DashboardService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        LocalDate today = LocalDate.now(clock);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("today", today)
                .addValue("weekAgo", Timestamp.from(clock.instant().minus(Duration.ofDays(7))));
        return jdbc.queryForObject("""
                SELECT (SELECT count(*) FROM books WHERE deleted_at IS NULL)                                    AS titles,
                       (SELECT coalesce(sum(total_quantity), 0) FROM books WHERE deleted_at IS NULL)            AS copies,
                       (SELECT coalesce(sum(available_quantity), 0) FROM books WHERE deleted_at IS NULL)        AS on_shelf,
                       (SELECT count(*) FROM loans WHERE returned_at IS NULL)                                   AS on_loan,
                       (SELECT count(*) FROM loans WHERE returned_at IS NULL AND due_date < :today)             AS overdue,
                       (SELECT count(*) FROM loans WHERE returned_at IS NULL AND due_date = :today)             AS due_today,
                       (SELECT coalesce(sum(amount), 0) FROM fines WHERE status = 'UNPAID')                     AS unpaid_fines,
                       (SELECT count(*) FROM fines WHERE status = 'UNPAID')                                     AS unpaid_fine_count,
                       (SELECT count(*) FROM members WHERE deleted_at IS NULL AND status = 'ACTIVE')            AS active_members,
                       (SELECT count(*) FROM loans WHERE issued_at >= :weekAgo)                                 AS issued_week,
                       (SELECT count(*) FROM loans WHERE returned_at >= :weekAgo)                               AS returned_week,
                       (SELECT count(*) FROM books WHERE deleted_at IS NULL AND created_at >= :weekAgo)         AS titles_week,
                       (SELECT count(*) FROM members WHERE deleted_at IS NULL AND created_at >= :weekAgo)       AS members_week
                """, params, (rs, i) -> new Summary(
                rs.getLong("titles"), rs.getLong("copies"), rs.getLong("on_shelf"), rs.getLong("on_loan"),
                rs.getLong("overdue"), rs.getLong("due_today"), rs.getBigDecimal("unpaid_fines"), rs.getLong("unpaid_fine_count"),
                rs.getLong("active_members"), rs.getLong("issued_week"), rs.getLong("returned_week"),
                rs.getLong("titles_week"), rs.getLong("members_week")));
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> loanTrend(int days) {
        LocalDate end = LocalDate.now(clock);
        LocalDate start = end.minusDays(days - 1L);
        Instant from = start.atStartOfDay(clock.getZone()).toInstant();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end)
                .addValue("from", Timestamp.from(from))
                .addValue("tz", clock.getZone().getId());
        return jdbc.query("""
                SELECT d.day::date AS day, coalesce(i.n, 0) AS issued, coalesce(r.n, 0) AS returned
                  FROM generate_series(CAST(:start AS date), CAST(:end AS date), interval '1 day') AS d(day)
                  LEFT JOIN (SELECT (issued_at AT TIME ZONE :tz)::date AS day, count(*) AS n
                               FROM loans WHERE issued_at >= :from GROUP BY 1) i ON i.day = d.day::date
                  LEFT JOIN (SELECT (returned_at AT TIME ZONE :tz)::date AS day, count(*) AS n
                               FROM loans WHERE returned_at >= :from GROUP BY 1) r ON r.day = d.day::date
                 ORDER BY d.day
                """, params, (rs, i) -> new TrendPoint(rs.getObject("day", LocalDate.class), rs.getLong("issued"), rs.getLong("returned")));
    }

    @Transactional(readOnly = true)
    public List<TopBook> topBooks(int limit, int days) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", Timestamp.from(clock.instant().minus(Duration.ofDays(days))))
                .addValue("limit", limit);
        return jdbc.query("""
                SELECT b.id, b.title, b.isbn13, b.cover_url, count(l.id) AS loans,
                       (SELECT string_agg(a.name, ', ' ORDER BY ba.position)
                          FROM book_authors ba JOIN authors a ON a.id = ba.author_id
                         WHERE ba.book_id = b.id) AS authors
                  FROM loans l
                  JOIN books b ON b.id = l.book_id
                 WHERE l.issued_at >= :from AND b.deleted_at IS NULL
                 GROUP BY b.id
                 ORDER BY loans DESC, b.title
                 LIMIT :limit
                """, params, (rs, i) -> new TopBook(rs.getLong("id"), rs.getString("title"), rs.getString("authors"),
                rs.getString("isbn13"), rs.getString("cover_url"), rs.getLong("loans")));
    }

    @Transactional(readOnly = true)
    public List<GenreShare> genreDistribution() {
        return jdbc.query("""
                SELECT g.id, g.name, count(b.id) AS titles, coalesce(sum(b.total_quantity), 0) AS copies
                  FROM genres g
                  JOIN books b ON b.genre_id = g.id AND b.deleted_at IS NULL
                 GROUP BY g.id, g.name
                 ORDER BY titles DESC, g.name
                """, new MapSqlParameterSource(), (rs, i) -> new GenreShare(rs.getInt("id"), rs.getString("name"),
                rs.getLong("titles"), rs.getLong("copies")));
    }

    public record Summary(long titles, long copies, long copiesOnShelf, long onLoan, long overdue, long dueToday,
                          BigDecimal unpaidFines, long unpaidFineCount, long activeMembers, long issuedThisWeek,
                          long returnedThisWeek, long titlesAddedThisWeek, long membersJoinedThisWeek) {
    }

    public record TrendPoint(LocalDate date, long issued, long returned) {
    }

    public record TopBook(long id, String title, String authors, String isbn13, String coverUrl, long loans) {
    }

    public record GenreShare(int id, String name, long titles, long copies) {
    }
}
