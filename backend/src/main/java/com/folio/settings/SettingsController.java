package com.folio.settings;

import com.folio.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Settings")
public class SettingsController {

    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
    }

    @GetMapping
    public SettingsService.Rules get() {
        return settings.current();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SettingsService.Rules update(@Valid @RequestBody SettingsService.UpdateRequest request,
                                        @AuthenticationPrincipal AuthenticatedUser actor) {
        return settings.update(request, actor);
    }
}
