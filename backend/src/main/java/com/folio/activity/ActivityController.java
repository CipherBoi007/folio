package com.folio.activity;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.PageResponse;
import com.folio.common.web.Pagination;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity")
@Tag(name = "Activity")
public class ActivityController {

    private static final Set<String> ENTITY_TYPES = Set.of("BOOK", "MEMBER", "LOAN", "FINE", "SETTINGS", "STAFF");

    private final ActivityService activity;

    public ActivityController(ActivityService activity) {
        this.activity = activity;
    }

    @GetMapping
    public PageResponse<ActivityEntry> list(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        if (entityType != null && !ENTITY_TYPES.contains(entityType)) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "Unknown entity type \"" + entityType + "\".");
        }
        var pageable = Pagination.pageable(page, size, null, Map.of(), Sort.unsorted());
        return activity.search(new ActivityService.ActivityFilter(actorId, entityType, entityId, from, to),
                pageable.getPageNumber(), pageable.getPageSize());
    }
}
