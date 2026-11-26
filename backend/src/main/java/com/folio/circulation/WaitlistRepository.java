package com.folio.circulation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    @EntityGraph(attributePaths = {"member", "book"})
    @Query("SELECT w FROM WaitlistEntry w WHERE w.book.id = :bookId AND w.status = com.folio.circulation.WaitlistEntry.Status.WAITING ORDER BY w.createdAt, w.id")
    List<WaitlistEntry> queueForBook(@Param("bookId") long bookId);

    @EntityGraph(attributePaths = {"member", "book"})
    @Query("SELECT w FROM WaitlistEntry w WHERE w.member.id = :memberId AND w.status = com.folio.circulation.WaitlistEntry.Status.WAITING ORDER BY w.createdAt")
    List<WaitlistEntry> waitingForMember(@Param("memberId") long memberId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.book.id = :bookId AND w.member.id = :memberId AND w.status = com.folio.circulation.WaitlistEntry.Status.WAITING")
    Optional<WaitlistEntry> findWaiting(@Param("bookId") long bookId, @Param("memberId") long memberId);

    @Query("SELECT count(w) > 0 FROM WaitlistEntry w WHERE w.book.id = :bookId AND w.status = com.folio.circulation.WaitlistEntry.Status.WAITING")
    boolean anyWaiting(@Param("bookId") long bookId);

    @Query("SELECT count(w) FROM WaitlistEntry w WHERE w.book.id = :bookId AND w.status = com.folio.circulation.WaitlistEntry.Status.WAITING AND (w.createdAt < :createdAt OR (w.createdAt = :createdAt AND w.id < :id))")
    long aheadOf(@Param("bookId") long bookId, @Param("createdAt") Instant createdAt, @Param("id") long id);
}
