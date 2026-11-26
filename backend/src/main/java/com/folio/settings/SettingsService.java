package com.folio.settings;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final LibrarySettingsRepository repository;
    private final ActivityService activity;
    private final Clock clock;

    SettingsService(LibrarySettingsRepository repository, ActivityService activity, Clock clock) {
        this.repository = repository;
        this.activity = activity;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Rules current() {
        return Rules.from(load());
    }

    @Transactional
    public Rules update(UpdateRequest request, AuthenticatedUser actor) {
        LibrarySettings settings = load();
        Rules before = Rules.from(settings);
        settings.update(request.loanDays(), request.renewalDays(), request.maxActiveLoans(), request.finePerDay(),
                request.fineBlockThreshold(), Instant.now(clock), actor.id());
        Rules after = Rules.from(settings);
        activity.record(actor, ActivityAction.SETTINGS_UPDATED, "SETTINGS", null, "Updated library rules",
                Map.of("before", before, "after", after));
        return after;
    }

    private LibrarySettings load() {
        return repository.findById(LibrarySettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("library_settings row is missing"));
    }

    public record Rules(int loanDays, int renewalDays, int maxActiveLoans, BigDecimal finePerDay,
                        BigDecimal fineBlockThreshold, Instant updatedAt) {

        static Rules from(LibrarySettings s) {
            return new Rules(s.getLoanDays(), s.getRenewalDays(), s.getMaxActiveLoans(), s.getFinePerDay(),
                    s.getFineBlockThreshold(), s.getUpdatedAt());
        }
    }

    public record UpdateRequest(
            @NotNull @Min(1) @Max(90) Integer loanDays,
            @NotNull @Min(1) @Max(90) Integer renewalDays,
            @NotNull @Min(1) @Max(20) Integer maxActiveLoans,
            @NotNull @DecimalMin(value = "0.01") @DecimalMax("10000") @Digits(integer = 8, fraction = 2) BigDecimal finePerDay,
            @NotNull @DecimalMin("0") @DecimalMax("100000") @Digits(integer = 8, fraction = 2) BigDecimal fineBlockThreshold) {
    }
}
