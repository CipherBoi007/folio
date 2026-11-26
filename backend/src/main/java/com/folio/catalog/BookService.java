package com.folio.catalog;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.catalog.CatalogDtos.BookDetail;
import com.folio.catalog.CatalogDtos.BookFilter;
import com.folio.catalog.CatalogDtos.BookRequest;
import com.folio.catalog.CatalogDtos.BookSummary;
import com.folio.catalog.CatalogDtos.GenreCount;
import com.folio.catalog.CatalogDtos.IsbnLookup;
import com.folio.catalog.CatalogDtos.LanguageRef;
import com.folio.catalog.CatalogDtos.StockInRequest;
import com.folio.catalog.CatalogDtos.StockOutRequest;
import com.folio.catalog.CatalogDtos.StockResult;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.PageResponse;
import com.folio.inventory.InventoryLedger;
import com.folio.inventory.StockChange;
import com.folio.inventory.TransactionType;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository books;
    private final GenreRepository genres;
    private final PublisherRepository publishers;
    private final AuthorRepository authors;
    private final LanguageRepository languages;
    private final InventoryLedger ledger;
    private final ActivityService activity;
    private final OpenLibraryClient openLibrary;
    private final Clock clock;

    public BookService(BookRepository books, GenreRepository genres, PublisherRepository publishers, AuthorRepository authors,
                       LanguageRepository languages, InventoryLedger ledger, ActivityService activity,
                       OpenLibraryClient openLibrary, Clock clock) {
        this.books = books;
        this.genres = genres;
        this.publishers = publishers;
        this.authors = authors;
        this.languages = languages;
        this.ledger = ledger;
        this.activity = activity;
        this.openLibrary = openLibrary;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<BookSummary> search(BookFilter filter, Pageable pageable) {
        Page<Book> page = books.findAll(BookSpecifications.matching(filter), pageable);
        return PageResponse.of(page, BookSummary::from);
    }

    @Transactional(readOnly = true)
    public List<BookSummary> quickSearch(String q, int limit) {
        return books.findAll(BookSpecifications.matching(new BookFilter(q, null, null, null)),
                PageRequest.of(0, limit, Sort.by("title"))).map(BookSummary::from).getContent();
    }

    @Transactional(readOnly = true)
    public void stream(BookFilter filter, Sort sort, Consumer<BookSummary> consumer) {
        books.findAll(BookSpecifications.matching(filter), sort).forEach(book -> consumer.accept(BookSummary.from(book)));
    }

    @Transactional(readOnly = true)
    public BookDetail get(long id) {
        Book book = books.findLive(id).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, id));
        return BookDetail.from(book, books.countActiveLoans(id));
    }

    @Transactional
    public BookDetail create(BookRequest request, AuthenticatedUser actor) {
        String isbn = Isbn.toIsbn13(request.isbn());
        ensureIsbnAvailable(isbn, null);
        Book book = new Book(isbn);
        describe(book, isbn, request);
        books.saveAndFlush(book);

        int initial = request.initialQuantity() == null ? 0 : request.initialQuantity();
        if (initial > 0) {
            StockChange change = book.addCopies(initial);
            ledger.record(book.getId(), TransactionType.INITIAL_STOCK, change, "Initial stock", null, null, actor);
        }
        activity.record(actor, ActivityAction.BOOK_CREATED, "BOOK", book.getId(),
                "Added \"%s\" with %d %s".formatted(book.getTitle(), initial, initial == 1 ? "copy" : "copies"),
                Map.of("isbn13", isbn, "copies", initial));
        log.info("Book {} created ({}), {} copies", book.getId(), isbn, initial);
        return BookDetail.from(book, 0);
    }

    @Transactional
    public BookDetail update(long id, BookRequest request, AuthenticatedUser actor) {
        Book book = books.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, id));
        if (request.version() != null && request.version() != book.getVersion()) {
            throw new ApiException(ErrorCode.CONCURRENT_MODIFICATION);
        }
        String isbn = Isbn.toIsbn13(request.isbn());
        ensureIsbnAvailable(isbn, id);
        describe(book, isbn, request);
        books.flush();
        activity.record(actor, ActivityAction.BOOK_UPDATED, "BOOK", id, "Updated \"%s\"".formatted(book.getTitle()), Map.of());
        return BookDetail.from(book, books.countActiveLoans(id));
    }

    @Transactional
    public void delete(long id, AuthenticatedUser actor) {
        Book book = books.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, id));
        long onLoan = books.countActiveLoans(id);
        if (onLoan > 0) {
            throw new ApiException(ErrorCode.BOOK_HAS_ACTIVE_LOANS,
                    "\"%s\" has %d %s on loan. Record the %s before deleting it."
                            .formatted(book.getTitle(), onLoan, onLoan == 1 ? "copy" : "copies", onLoan == 1 ? "return" : "returns"),
                    Map.of("activeLoans", onLoan));
        }
        book.markDeleted(Instant.now(clock));
        activity.record(actor, ActivityAction.BOOK_DELETED, "BOOK", id, "Deleted \"%s\"".formatted(book.getTitle()),
                Map.of("isbn13", book.getIsbn13()));
    }

    @Transactional
    public StockResult stockIn(long id, StockInRequest request, AuthenticatedUser actor) {
        Book book = books.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, id));
        StockChange change = book.addCopies(request.quantity());
        ledger.record(id, TransactionType.STOCK_IN, change, request.reason(), blankToNull(request.note()), null, actor);
        activity.record(actor, ActivityAction.STOCK_ADDED, "BOOK", id,
                "Added %d %s of \"%s\"".formatted(request.quantity(), request.quantity() == 1 ? "copy" : "copies", book.getTitle()),
                Map.of("quantity", request.quantity(), "reason", request.reason()));
        return new StockResult(id, book.getTotalQuantity(), book.getAvailableQuantity());
    }

    @Transactional
    public StockResult stockOut(long id, StockOutRequest request, AuthenticatedUser actor) {
        Book book = books.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, id));
        StockChange change = book.removeCopies(request.quantity());
        ledger.record(id, TransactionType.STOCK_OUT, change, request.reason().name(), blankToNull(request.note()), null, actor);
        activity.record(actor, ActivityAction.STOCK_REMOVED, "BOOK", id,
                "Removed %d %s of \"%s\" (%s)".formatted(request.quantity(), request.quantity() == 1 ? "copy" : "copies",
                        book.getTitle(), request.reason().name().toLowerCase()),
                Map.of("quantity", request.quantity(), "reason", request.reason().name()));
        return new StockResult(id, book.getTotalQuantity(), book.getAvailableQuantity());
    }

    @Transactional(readOnly = true)
    public List<GenreCount> genres() {
        return genres.findAllWithCounts();
    }

    @Transactional(readOnly = true)
    public List<LanguageRef> languages() {
        return languages.findAll(Sort.by("name")).stream().map(l -> new LanguageRef(l.getCode(), l.getName())).toList();
    }

    @Transactional(readOnly = true)
    public IsbnLookup lookup(String rawIsbn) {
        String isbn = Isbn.toIsbn13(rawIsbn);
        Long existing = books.findByIsbn13(isbn).map(Book::getId).orElse(null);
        return openLibrary.lookup(isbn)
                .map(m -> new IsbnLookup(isbn, true, existing, m.title(), m.subtitle(), m.authors(), m.publisher(),
                        m.publishedYear(), m.pages(), m.coverUrl(), m.subjects()))
                .orElseGet(() -> new IsbnLookup(isbn, false, existing, null, null, List.of(), null, null, null, null, List.of()));
    }

    private void ensureIsbnAvailable(String isbn, Long selfId) {
        books.findByIsbn13(isbn).filter(existing -> !existing.getId().equals(selfId)).ifPresent(existing -> {
            String where = existing.isDeleted() ? " (a deleted record keeps its ISBN reserved)" : "";
            throw new ApiException(ErrorCode.DUPLICATE_ISBN,
                    "ISBN %s is already used by \"%s\"%s.".formatted(isbn, existing.getTitle(), where),
                    Map.of("bookId", existing.getId()));
        });
    }

    private void describe(Book book, String isbn, BookRequest r) {
        if (r.publishedYear() != null && r.publishedYear() > Year.now(clock).getValue() + 1) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "The publication year can't be in the future.");
        }
        Language language = languages.findById(r.languageCode())
                .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_FAILED, "Unknown language \"" + r.languageCode() + "\"."));
        Genre genre = genres.findByNameIgnoreCase(r.genre().trim()).orElseGet(() -> genres.save(new Genre(r.genre().trim())));
        Publisher publisher = blankToNull(r.publisher()) == null ? null
                : publishers.findByNameIgnoreCase(r.publisher().trim()).orElseGet(() -> publishers.save(new Publisher(r.publisher().trim())));

        Map<String, Author> unique = new LinkedHashMap<>();
        for (String name : r.authors()) {
            String clean = name.trim().replaceAll("\\s+", " ");
            unique.computeIfAbsent(clean.toLowerCase(), k -> authors.findByNameIgnoreCase(clean).orElseGet(() -> authors.save(new Author(clean))));
        }

        book.describe(isbn, r.title().trim(), blankToNull(r.subtitle()), new ArrayList<>(unique.values()), genre, publisher, language,
                r.publishedYear() == null ? null : r.publishedYear().shortValue(), r.pages(),
                blankToNull(r.shelfLocation()) == null ? null : r.shelfLocation().trim().toUpperCase(),
                blankToNull(r.description()), blankToNull(r.coverUrl()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
