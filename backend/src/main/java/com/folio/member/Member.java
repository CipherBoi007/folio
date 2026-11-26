package com.folio.member;

import com.folio.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "members")
public class Member extends AuditableEntity {

    @Column(name = "membership_number", nullable = false, updatable = false, length = 20)
    private String membershipNumber;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 10, columnDefinition = "char(10)")
    private String phone;

    @Column(name = "address_line", length = 300)
    private String addressLine;

    @Column(length = 80)
    private String city;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "postal_code", length = 6, columnDefinition = "char(6)")
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "joined_on", nullable = false, updatable = false)
    private LocalDate joinedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Member() {
    }

    Member(String membershipNumber, LocalDate joinedOn) {
        this.membershipNumber = membershipNumber;
        this.joinedOn = joinedOn;
    }

    void describe(String fullName, String email, String phone, String addressLine, String city, String postalCode) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.addressLine = addressLine;
        this.city = city;
        this.postalCode = postalCode;
    }

    void changeStatus(MemberStatus status) {
        this.status = status;
    }

    void markDeleted(Instant now) {
        this.deletedAt = now;
        this.status = MemberStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE && deletedAt == null;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public LocalDate getJoinedOn() {
        return joinedOn;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
