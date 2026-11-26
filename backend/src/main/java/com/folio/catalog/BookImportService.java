package com.folio.catalog;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.catalog.CatalogDtos.BookRequest;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Bulk catalogue import from CSV. Every row is validated and, unless it's a dry run, created in its own
 * transaction so one bad row never blocks the rest. The report lists the outcome of each row.
 */
@Service
public class BookImportService {

    static final List<String> COLUMNS = List.of("isbn", "title", "authors", "genre", "language", "publisher", "year", "pages", "shelf", "copies");
    private static final int MAX_ROWS = 2000;

    private final BookService books;
    private final BookRepository bookRepository;
    private final LanguageRepository languages;
    private final Validator validator;
    private final ActivityService activity;
    private final TransactionTemplate tx;

    public BookImportService(BookService books, BookRepository bookRepository, LanguageRepository languages, Validator validator,
                             ActivityService activity, TransactionTemplate tx) {
        this.books = books;
        this.bookRepository = bookRepository;
        this.languages = languages;
        this.validator = validator;
        this.activity = activity;
        this.tx = tx;
    }

    public Report importCsv(InputStream input, boolean dryRun, AuthenticatedUser actor) throws IOException {
        List<List<String>> rows = parse(input);
        if (rows.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "The file is empty.");
        }
        Map<String, Integer> header = new HashMap<>();
        List<String> names = rows.getFirst();
        for (int i = 0; i < names.size(); i++) {
            header.put(names.get(i).trim().toLowerCase(Locale.ROOT).replace("﻿", ""), i);
        }
        if (!header.containsKey("isbn") || !header.containsKey("title") || !header.containsKey("authors") || !header.containsKey("genre")) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "The header must include isbn, title, authors and genre. Columns: " + String.join(", ", COLUMNS) + ".");
        }
        if (rows.size() - 1 > MAX_ROWS) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Import up to %d books at a time.".formatted(MAX_ROWS));
        }

        Map<String, String> languageCodes = new HashMap<>();
        languages.findAll().forEach(l -> {
            languageCodes.put(l.getCode().toLowerCase(Locale.ROOT), l.getCode());
            languageCodes.put(l.getName().toLowerCase(Locale.ROOT), l.getCode());
        });

        List<RowResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int created = 0;
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.stream().allMatch(String::isBlank)) {
                continue;
            }
            int line = i + 1;
            String isbnRaw = cell(row, header, "isbn");
            String title = cell(row, header, "title");
            try {
                BookRequest request = toRequest(row, header, languageCodes);
                Set<ConstraintViolation<BookRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new ApiException(ErrorCode.VALIDATION_FAILED, violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().collect(Collectors.joining("; ")));
                }
                String isbn = Isbn.toIsbn13(request.isbn());
                if (!seen.add(isbn)) {
                    throw new ApiException(ErrorCode.DUPLICATE_ISBN, "ISBN %s appears earlier in this file.".formatted(isbn));
                }
                if (dryRun) {
                    if (bookRepository.findByIsbn13(isbn).isPresent()) {
                        throw new ApiException(ErrorCode.DUPLICATE_ISBN, "ISBN %s is already in the catalogue.".formatted(isbn));
                    }
                    results.add(new RowResult(line, isbn, request.title(), "READY", null));
                } else {
                    books.create(request, actor);
                    results.add(new RowResult(line, isbn, request.title(), "CREATED", null));
                    created++;
                }
            } catch (ApiException e) {
                results.add(new RowResult(line, isbnRaw, title, "FAILED", e.getMessage()));
            }
        }

        int failed = (int) results.stream().filter(r -> r.status().equals("FAILED")).count();
        if (!dryRun && created > 0) {
            int total = created;
            tx.executeWithoutResult(status -> activity.record(actor, ActivityAction.BOOKS_IMPORTED, "BOOK", null,
                    "Imported %d %s from CSV".formatted(total, total == 1 ? "book" : "books"), Map.of("created", total, "failed", failed)));
        }
        return new Report(dryRun, results.size(), created, failed, results);
    }

    private static BookRequest toRequest(List<String> row, Map<String, Integer> header, Map<String, String> languageCodes) {
        String language = cell(row, header, "language");
        String code = language.isBlank() ? "en" : languageCodes.get(language.toLowerCase(Locale.ROOT));
        if (code == null) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Unknown language \"%s\".".formatted(language));
        }
        List<String> authors = Arrays.stream(cell(row, header, "authors").split(";")).map(String::trim).filter(a -> !a.isEmpty()).toList();
        return new BookRequest(cell(row, header, "isbn"), cell(row, header, "title"), null, authors, cell(row, header, "genre"),
                blankToNull(cell(row, header, "publisher")), code, number(row, header, "year"), number(row, header, "pages"),
                blankToNull(cell(row, header, "shelf")), null, null, number(row, header, "copies") == null ? 1 : number(row, header, "copies"), null);
    }

    private static Integer number(List<String> row, Map<String, Integer> header, String column) {
        String value = cell(row, header, column);
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "%s must be a whole number, got \"%s\".".formatted(column, value));
        }
    }

    private static String cell(List<String> row, Map<String, Integer> header, String column) {
        Integer index = header.get(column);
        return index == null || index >= row.size() ? "" : row.get(index).trim();
    }

    private static String blankToNull(String value) {
        return value.isBlank() ? null : value;
    }

    /** RFC 4180: quoted fields, escaped quotes and line breaks inside quotes. */
    static List<List<String>> parse(InputStream input) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            int c;
            while ((c = reader.read()) != -1) {
                char ch = (char) c;
                if (quoted) {
                    if (ch == '"') {
                        reader.mark(1);
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            quoted = false;
                            if (next != -1) {
                                reader.reset();
                            }
                        }
                    } else {
                        field.append(ch);
                    }
                } else if (ch == '"') {
                    quoted = true;
                } else if (ch == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                } else if (ch == '\n') {
                    row.add(field.toString());
                    field.setLength(0);
                    rows.add(row);
                    row = new ArrayList<>();
                } else if (ch != '\r') {
                    field.append(ch);
                }
            }
            if (field.length() > 0 || !row.isEmpty()) {
                row.add(field.toString());
                rows.add(row);
            }
        }
        return rows;
    }

    public record RowResult(int row, String isbn, String title, String status, String message) {
    }

    public record Report(boolean dryRun, int rows, int created, int failed, List<RowResult> results) {
    }
}
