package com.folio.circulation;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.catalog.Book;
import com.folio.catalog.BookRepository;
import com.folio.circulation.CirculationDtos.MemberRef;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.member.Member;
import com.folio.member.MemberRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Queue for out-of-stock titles. A place in the queue is informational: returned copies are not held. */
@Service
public class WaitlistService {

    private final WaitlistRepository waitlist;
    private final BookRepository books;
    private final MemberRepository members;
    private final LoanRepository loans;
    private final ActivityService activity;
    private final Clock clock;

    public WaitlistService(WaitlistRepository waitlist, BookRepository books, MemberRepository members, LoanRepository loans,
                           ActivityService activity, Clock clock) {
        this.waitlist = waitlist;
        this.books = books;
        this.members = members;
        this.loans = loans;
        this.activity = activity;
        this.clock = clock;
    }

    @Transactional
    public Entry join(long bookId, long memberId, AuthenticatedUser actor) {
        Book book = books.findLiveForUpdate(bookId).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, bookId));
        Member member = members.findLive(memberId).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, memberId));
        if (!member.isActive()) {
            throw new ApiException(ErrorCode.MEMBER_INACTIVE, "%s's membership isn't active.".formatted(member.getFullName()));
        }
        if (book.getAvailableQuantity() > 0) {
            throw new ApiException(ErrorCode.BOOK_AVAILABLE);
        }
        if (loans.existsActive(memberId, bookId)) {
            throw new ApiException(ErrorCode.ALREADY_BORROWED, "%s already has \"%s\" on loan.".formatted(member.getFullName(), book.getTitle()));
        }
        if (waitlist.findWaiting(bookId, memberId).isPresent()) {
            throw new ApiException(ErrorCode.ALREADY_WAITLISTED, "%s is already waiting for \"%s\".".formatted(member.getFullName(), book.getTitle()));
        }
        WaitlistEntry entry = waitlist.saveAndFlush(new WaitlistEntry(book, member, Instant.now(clock), actor.id()));
        activity.record(actor, ActivityAction.WAITLIST_JOINED, "BOOK", bookId,
                "Added %s to the waitlist for \"%s\"".formatted(member.getFullName(), book.getTitle()), Map.of("memberId", memberId));
        return view(entry, (int) waitlist.aheadOf(bookId, entry.getCreatedAt(), entry.getId()) + 1);
    }

    @Transactional
    public void cancel(long entryId, AuthenticatedUser actor) {
        WaitlistEntry entry = waitlist.findById(entryId)
                .filter(e -> e.getStatus() == WaitlistEntry.Status.WAITING)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.NOT_FOUND, entryId));
        entry.resolve(WaitlistEntry.Status.CANCELLED, Instant.now(clock));
        activity.record(actor, ActivityAction.WAITLIST_CANCELLED, "BOOK", entry.getBook().getId(),
                "Removed %s from the waitlist for \"%s\"".formatted(entry.getMember().getFullName(), entry.getBook().getTitle()), Map.of());
    }

    @Transactional(readOnly = true)
    public List<Entry> forBook(long bookId) {
        AtomicInteger position = new AtomicInteger();
        return waitlist.queueForBook(bookId).stream().map(e -> view(e, position.incrementAndGet())).toList();
    }

    @Transactional(readOnly = true)
    public List<Entry> forMember(long memberId) {
        return waitlist.waitingForMember(memberId).stream()
                .map(e -> view(e, (int) waitlist.aheadOf(e.getBook().getId(), e.getCreatedAt(), e.getId()) + 1))
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    boolean anyWaiting(long bookId) {
        return waitlist.anyWaiting(bookId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    void fulfil(long bookId, long memberId) {
        waitlist.findWaiting(bookId, memberId).ifPresent(e -> e.resolve(WaitlistEntry.Status.FULFILLED, Instant.now(clock)));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    Optional<MemberRef> nextInQueue(long bookId) {
        return waitlist.queueForBook(bookId).stream().findFirst().map(e -> MemberRef.from(e.getMember()));
    }

    private static Entry view(WaitlistEntry e, int position) {
        return new Entry(e.getId(), position, e.getBook().getId(), e.getBook().getTitle(), MemberRef.from(e.getMember()), e.getCreatedAt());
    }

    public record Entry(long id, int position, long bookId, String bookTitle, MemberRef member, Instant createdAt) {
    }
}
