package com.folio.circulation;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findAll(Specification<Loan> spec, Sort sort);

    @EntityGraph(attributePaths = {"book", "member"})
    @Query("SELECT l FROM Loan l WHERE l.id = :id")
    Optional<Loan> findWithDetails(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :id")
    Optional<Loan> findForUpdate(@Param("id") long id);

    @Query("SELECT count(l) FROM Loan l WHERE l.member.id = :memberId AND l.returnedAt IS NULL")
    long countActive(@Param("memberId") long memberId);

    @Query("SELECT count(l) FROM Loan l WHERE l.member.id = :memberId AND l.returnedAt IS NULL AND l.dueDate < :today")
    long countOverdue(@Param("memberId") long memberId, @Param("today") LocalDate today);

    @Query("SELECT count(l) > 0 FROM Loan l WHERE l.member.id = :memberId AND l.book.id = :bookId AND l.returnedAt IS NULL")
    boolean existsActive(@Param("memberId") long memberId, @Param("bookId") long bookId);
}
