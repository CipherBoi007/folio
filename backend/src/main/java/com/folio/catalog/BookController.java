package com.folio.catalog;

import com.folio.auth.AuthenticatedUser;
import com.folio.catalog.CatalogDtos.Availability;
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
import com.folio.common.csv.CsvWriter;
import com.folio.common.web.PageResponse;
import com.folio.common.web.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books")
public class BookController {

    private static final Map<String, String> SORTS = Map.of(
            "title", "title",
            "year", "publishedYear",
            "available", "availableQuantity",
            "total", "totalQuantity",
            "added", "createdAt",
            "updated", "updatedAt");
    private static final Sort DEFAULT_SORT = Sort.by("title").and(Sort.by("id"));

    private static final long MAX_IMPORT_BYTES = 2 * 1024 * 1024;

    private final BookService books;
    private final BookImportService importer;

    public BookController(BookService books, BookImportService importer) {
        this.books = books;
        this.importer = importer;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import books from CSV (columns: isbn,title,authors,genre,language,publisher,year,pages,shelf,copies)")
    public BookImportService.Report importCsv(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "false") boolean dryRun,
                                             @AuthenticationPrincipal AuthenticatedUser actor) throws IOException {
        if (file.isEmpty() || file.getSize() > MAX_IMPORT_BYTES) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Choose a CSV file under 2 MB.");
        }
        return importer.importCsv(file.getInputStream(), dryRun, actor);
    }

    @GetMapping
    public PageResponse<BookSummary> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Integer> genreId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Availability availability,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return books.search(new BookFilter(q, genreId, language, availability), Pagination.pageable(page, size, sort, SORTS, DEFAULT_SORT));
    }

    @GetMapping("/export.csv")
    public void export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Integer> genreId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Availability availability,
            HttpServletResponse response) throws IOException {
        CsvWriter csv = CsvWriter.attachment(response, "folio-books.csv")
                .row("ISBN", "Title", "Subtitle", "Authors", "Genre", "Language", "Publisher", "Year", "Shelf", "Total", "Available");
        books.stream(new BookFilter(q, genreId, language, availability), DEFAULT_SORT, b -> csv.row(
                b.isbn13(), b.title(), b.subtitle(), String.join("; ", b.authors()), b.genre().name(), b.language().name(),
                b.publisher(), b.publishedYear(), b.shelfLocation(), b.totalQuantity(), b.availableQuantity()));
        csv.flush();
    }

    @GetMapping("/genres")
    public List<GenreCount> genres() {
        return books.genres();
    }

    @GetMapping("/languages")
    public List<LanguageRef> languages() {
        return books.languages();
    }

    @GetMapping("/lookup")
    @Operation(summary = "Look up book metadata by ISBN (Open Library) and report whether it is already catalogued")
    public IsbnLookup lookup(@RequestParam String isbn) {
        return books.lookup(isbn);
    }

    @GetMapping("/{id}")
    public BookDetail get(@PathVariable long id) {
        return books.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDetail create(@Valid @RequestBody BookRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return books.create(request, actor);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit metadata. Quantities change only through stock and circulation endpoints.")
    public BookDetail update(@PathVariable long id, @Valid @RequestBody BookRequest request,
                             @AuthenticationPrincipal AuthenticatedUser actor) {
        return books.update(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        books.delete(id, actor);
    }

    @PostMapping("/{id}/stock/in")
    public StockResult stockIn(@PathVariable long id, @Valid @RequestBody StockInRequest request,
                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return books.stockIn(id, request, actor);
    }

    @PostMapping("/{id}/stock/out")
    @PreAuthorize("hasRole('ADMIN')")
    public StockResult stockOut(@PathVariable long id, @Valid @RequestBody StockOutRequest request,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return books.stockOut(id, request, actor);
    }
}
