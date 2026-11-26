package com.folio.catalog;

import jakarta.persistence.LockModeType;
import java.util.Collection;
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

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Override
    @EntityGraph(attributePaths = {"genre", "publisher", "language"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"genre", "publisher", "language"})
    List<Book> findAll(Specification<Book> spec, Sort sort);

    @EntityGraph(attributePaths = {"genre", "publisher", "language"})
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Book> findLive(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Book> findLiveForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") long id);

    @EntityGraph(attributePaths = {"genre", "publisher", "language"})
    @Query("SELECT b FROM Book b WHERE b.id IN :ids")
    List<Book> findAllWithDetails(@Param("ids") Collection<Long> ids);

    Optional<Book> findByIsbn13(String isbn13);

    @Query(value = "SELECT count(*) FROM loans WHERE book_id = :bookId AND returned_at IS NULL", nativeQuery = true)
    long countActiveLoans(@Param("bookId") long bookId);
}
