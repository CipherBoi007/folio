package com.folio.member;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Member> findLive(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Member> findLiveForUpdate(@Param("id") long id);

    Optional<Member> findByEmail(String email);

    @Query(value = "SELECT nextval('membership_number_seq')", nativeQuery = true)
    long nextMembershipSequence();

    @Query(value = """
            SELECT l.member_id AS memberId,
                   count(*) AS activeLoans,
                   count(*) FILTER (WHERE l.due_date < :today) AS overdueLoans
              FROM loans l
             WHERE l.returned_at IS NULL AND l.member_id IN (:ids)
             GROUP BY l.member_id
            """, nativeQuery = true)
    List<LoanCounts> loanCounts(@Param("ids") Collection<Long> ids, @Param("today") LocalDate today);

    @Query(value = """
            SELECT l.member_id AS memberId, sum(f.amount) AS unpaid
              FROM fines f
              JOIN loans l ON l.id = f.loan_id
             WHERE f.status = 'UNPAID' AND l.member_id IN (:ids)
             GROUP BY l.member_id
            """, nativeQuery = true)
    List<FineTotal> unpaidFines(@Param("ids") Collection<Long> ids);

    @Query(value = "SELECT count(*) FROM loans WHERE member_id = :id", nativeQuery = true)
    long countAllLoans(@Param("id") long id);

    interface LoanCounts {
        Long getMemberId();

        Long getActiveLoans();

        Long getOverdueLoans();
    }

    interface FineTotal {
        Long getMemberId();

        BigDecimal getUnpaid();
    }
}
