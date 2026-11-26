package com.folio.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class MemberDtos {

    private MemberDtos() {
    }

    public record MemberSummary(
            long id,
            String membershipNumber,
            String fullName,
            String email,
            String phone,
            String city,
            MemberStatus status,
            LocalDate joinedOn,
            long activeLoans,
            long overdueLoans,
            BigDecimal unpaidFines) {
    }

    public record MemberDetail(
            long id,
            String membershipNumber,
            String fullName,
            String email,
            String phone,
            String addressLine,
            String city,
            String postalCode,
            MemberStatus status,
            LocalDate joinedOn,
            long activeLoans,
            long overdueLoans,
            long totalLoans,
            BigDecimal unpaidFines,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record MemberRequest(
            @NotBlank(message = "Enter the member's name.") @Size(max = 120) String fullName,
            @NotBlank(message = "Enter an email address.") @Email(message = "Enter a valid email address.") @Size(max = 254) String email,
            @NotBlank(message = "Enter a mobile number.") @Size(max = 20) String phone,
            @Size(max = 300) String addressLine,
            @Size(max = 80) String city,
            @Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "Enter a 6-digit PIN code.") String postalCode) {
    }

    public record StatusRequest(@NotNull(message = "Choose a status.") MemberStatus status) {
    }
}
