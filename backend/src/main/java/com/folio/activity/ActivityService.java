package com.folio.activity;

import com.folio.auth.AuthenticatedUser;
import com.folio.common.web.PageResponse;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ActivityService {

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    public ActivityService(NamedParameterJdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /** Joins the caller's transaction: the audit entry commits or rolls back with the change it describes. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuthenticatedUser actor, ActivityAction action, String entityType, Long entityId, String summary,
                       Map<String, ?> metadata) {
        jdbc.update("""
                INSERT INTO activity_log (actor_id, action, entity_type, entity_id, summary, metadata, created_at)
                VALUES (:actor, :action, :entityType, :entityId, :summary, CAST(:metadata AS jsonb), :createdAt)
                """, new MapSqlParameterSource()
                .addValue("actor", actor.id())
                .addValue("action", action.name())
                .addValue("entityType", entityType)
                .addValue("entityId", entityId)
                .addValue("summary", summary)
                .addValue("metadata", json.writeValueAsString(metadata == null ? Map.of() : metadata))
                .addValue("createdAt", Timestamp.from(Instant.now(clock))));
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityEntry> search(ActivityFilter filter, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (filter.actorId() != null) {
            where.append(" AND a.actor_id = :actorId");
            params.addValue("actorId", filter.actorId());
        }
        if (filter.entityType() != null) {
            where.append(" AND a.entity_type = :entityType");
            params.addValue("entityType", filter.entityType());
        }
        if (filter.entityId() != null) {
            where.append(" AND a.entity_id = :entityId");
            params.addValue("entityId", filter.entityId());
        }
        if (filter.from() != null) {
            where.append(" AND a.created_at >= :from");
            params.addValue("from", Timestamp.from(filter.from()));
        }
        if (filter.to() != null) {
            where.append(" AND a.created_at < :to");
            params.addValue("to", Timestamp.from(filter.to()));
        }
        Long total = jdbc.queryForObject("SELECT count(*) FROM activity_log a" + where, params, Long.class);
        params.addValue("limit", size).addValue("offset", (long) page * size);
        List<ActivityEntry> content = new ArrayList<>(jdbc.query("""
                SELECT a.id, a.action, a.entity_type, a.entity_id, a.summary, a.metadata::text AS metadata, a.created_at,
                       u.id AS actor_id, u.name AS actor_name, u.role AS actor_role
                  FROM activity_log a
                  JOIN users u ON u.id = a.actor_id
                """ + where + " ORDER BY a.created_at DESC, a.id DESC LIMIT :limit OFFSET :offset", params,
                (rs, i) -> new ActivityEntry(
                        rs.getLong("id"),
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getObject("entity_id", Long.class),
                        rs.getString("summary"),
                        json.readValue(rs.getString("metadata"), Map.class),
                        rs.getTimestamp("created_at").toInstant(),
                        new ActivityEntry.Actor(rs.getLong("actor_id"), rs.getString("actor_name"), rs.getString("actor_role")))));
        long totalElements = total == null ? 0 : total;
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    public record ActivityFilter(Long actorId, String entityType, Long entityId, Instant from, Instant to) {
    }
}
