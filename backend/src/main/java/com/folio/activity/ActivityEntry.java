package com.folio.activity;

import java.time.Instant;
import java.util.Map;

public record ActivityEntry(
        long id, String action, String entityType, Long entityId, String summary, Map<?, ?> metadata, Instant createdAt, Actor actor) {

    public record Actor(long id, String name, String role) {
    }
}
