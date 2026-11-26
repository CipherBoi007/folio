package com.folio.auth;

import com.folio.auth.AuthDtos.ChangePasswordRequest;
import com.folio.auth.AuthDtos.CreateStaffRequest;
import com.folio.auth.AuthDtos.ResetPasswordRequest;
import com.folio.auth.AuthDtos.StaffResponse;
import com.folio.auth.AuthDtos.UpdateStaffRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Staff")
public class StaffController {

    private final StaffService staff;

    public StaffController(StaffService staff) {
        this.staff = staff;
    }

    @GetMapping("/staff")
    public List<StaffResponse> list() {
        return staff.list();
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse create(@Valid @RequestBody CreateStaffRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return staff.create(request, actor);
    }

    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse update(@PathVariable long id, @Valid @RequestBody UpdateStaffRequest request,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return staff.update(id, request, actor);
    }

    @PostMapping("/staff/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void resetPassword(@PathVariable long id, @Valid @RequestBody ResetPasswordRequest request,
                              @AuthenticationPrincipal AuthenticatedUser actor) {
        staff.resetPassword(id, request.temporaryPassword(), actor);
    }

    @PostMapping("/auth/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        staff.changeOwnPassword(actor, request.currentPassword(), request.newPassword());
    }
}
