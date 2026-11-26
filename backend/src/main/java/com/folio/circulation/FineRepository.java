package com.folio.circulation;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface FineRepository extends JpaRepository<Fine, Long>, JpaSpecificationExecutor<Fine> {

    @Override
    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    Page<Fine> findAll(Specification<Fine> spec, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Fine f WHERE f.id = :id")
    Optional<Fine> findForUpdate(@Param("id") long id);

    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    @Query("SELECT f FROM Fine f WHERE f.id = :id")
    Optional<Fine> findWithDetails(@Param("id") long id);

    Optional<Fine> findByLoanId(long loanId);

    @Query("SELECT f FROM Fine f WHERE f.loan.id IN :loanIds")
    List<Fine> findByLoanIds(@Param("loanIds") List<Long> loanIds);

    @Query("SELECT coalesce(sum(f.amount), 0) FROM Fine f WHERE f.loan.member.id = :memberId AND f.status = com.folio.circulation.FineStatus.UNPAID")
    BigDecimal unpaidTotal(@Param("memberId") long memberId);

    @Query("""
            SELECT f.status AS status, count(f) AS count, coalesce(sum(f.amount), 0) AS total
              FROM Fine f GROUP BY f.status
            """)
    List<StatusTotal> totalsByStatus();

    interface StatusTotal {
        FineStatus getStatus();

        Long getCount();

        BigDecimal getTotal();
    }
}
