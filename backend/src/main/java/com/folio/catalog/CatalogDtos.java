package com.folio.catalog;

import com.folio.inventory.StockOutReason;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record Ref(Number id, String name) {
    }

    public record LanguageRef(String code, String name) {
    }

    public record GenreCount(Integer id, String name, long titles) {
    }

    public enum Availability {
        IN_STOCK, LOW, OUT
    }

    public record BookSummary(
            long id,
            String isbn13,
            String title,
            String subtitle,
            List<String> authors,
            Ref genre,
            LanguageRef language,
            String publisher,
            Short publishedYear,
            String shelfLocation,
            String coverUrl,
            int totalQuantity,
            int availableQuantity) {

        static BookSummary from(Book b) {
            return new BookSummary(b.getId(), b.getIsbn13(), b.getTitle(), b.getSubtitle(),
                    b.getAuthors().stream().map(Author::getName).toList(),
                    new Ref(b.getGenre().getId(), b.getGenre().getName()),
                    new LanguageRef(b.getLanguage().getCode(), b.getLanguage().getName()),
                    b.getPublisher() == null ? null : b.getPublisher().getName(),
                    b.getPublishedYear(), b.getShelfLocation(), b.getCoverUrl(), b.getTotalQuantity(), b.getAvailableQuantity());
        }
    }

    public record BookDetail(
            long id,
            String isbn13,
            String title,
            String subtitle,
            List<String> authors,
            Ref genre,
            LanguageRef language,
            String publisher,
            Short publishedYear,
            Integer pages,
            String shelfLocation,
            String description,
            String coverUrl,
            int totalQuantity,
            int availableQuantity,
            long activeLoans,
            long version,
            Instant createdAt,
            Instant updatedAt) {

        static BookDetail from(Book b, long activeLoans) {
            return new BookDetail(b.getId(), b.getIsbn13(), b.getTitle(), b.getSubtitle(),
                    b.getAuthors().stream().map(Author::getName).toList(),
                    new Ref(b.getGenre().getId(), b.getGenre().getName()),
                    new LanguageRef(b.getLanguage().getCode(), b.getLanguage().getName()),
                    b.getPublisher() == null ? null : b.getPublisher().getName(),
                    b.getPublishedYear(), b.getPages(), b.getShelfLocation(), b.getDescription(), b.getCoverUrl(),
                    b.getTotalQuantity(), b.getAvailableQuantity(), activeLoans, b.getVersion(), b.getCreatedAt(), b.getUpdatedAt());
        }
    }

    public record BookRequest(
            @NotBlank(message = "Enter the ISBN.") @Size(max = 20) String isbn,
            @NotBlank(message = "Enter the title.") @Size(max = 300) String title,
            @Size(max = 300) String subtitle,
            @NotEmpty(message = "Add at least one author.") @Size(max = 10, message = "Up to 10 authors.")
            List<@NotBlank(message = "Author names can't be blank.") @Size(max = 200) String> authors,
            @NotBlank(message = "Choose a genre.") @Size(max = 60) String genre,
            @Size(max = 200) String publisher,
            @NotBlank(message = "Choose a language.") @Size(max = 8) String languageCode,
            @Min(value = 1000, message = "Enter a four-digit year.") Integer publishedYear,
            @Min(value = 1, message = "Pages must be at least 1.") @Max(value = 20000) Integer pages,
            @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9 ./-]*$", message = "Use letters, numbers and dashes, e.g. A-03.")
            String shelfLocation,
            @Size(max = 5000) String description,
            @Size(max = 500) @Pattern(regexp = "^https://\\S+$", message = "Cover must be an https URL.") String coverUrl,
            @Min(value = 0, message = "Copies can't be negative.") @Max(value = 1000) Integer initialQuantity,
            Long version) {
    }

    public record StockInRequest(
            @NotNull @Min(value = 1, message = "Add at least 1 copy.") @Max(value = 1000) Integer quantity,
            @NotBlank(message = "Give a reason, e.g. \"Purchase\" or \"Donation\".") @Size(max = 40) String reason,
            @Size(max = 500) String note) {
    }

    public record StockOutRequest(
            @NotNull @Min(value = 1, message = "Remove at least 1 copy.") @Max(value = 1000) Integer quantity,
            @NotNull(message = "Choose a reason.") StockOutReason reason,
            @Size(max = 500) String note) {
    }

    public record StockResult(long bookId, int totalQuantity, int availableQuantity) {
    }

    public record IsbnLookup(
            String isbn13,
            boolean found,
            Long existingBookId,
            String title,
            String subtitle,
            List<String> authors,
            String publisher,
            Integer publishedYear,
            Integer pages,
            String coverUrl,
            List<String> subjects) {
    }

    record BookFilter(String q, List<Integer> genreIds, String languageCode, Availability availability) {
    }
}
