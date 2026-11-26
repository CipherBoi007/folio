package com.folio.catalog;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.persistence.AuditableEntity;
import com.folio.inventory.StockChange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "books")
public class Book extends AuditableEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "isbn13", nullable = false, length = 13, columnDefinition = "char(13)")
    private String isbn13;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
    @OrderColumn(name = "position")
    @ListIndexBase(1)
    @BatchSize(size = 100)
    private List<Author> authors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_code", nullable = false)
    private Language language;

    @Column(name = "published_year")
    private Short publishedYear;

    private Integer pages;

    @Column(name = "shelf_location", length = 20)
    private String shelfLocation;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Version
    private long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Book() {
    }

    Book(String isbn13) {
        this.isbn13 = isbn13;
    }

    void describe(String isbn13, String title, String subtitle, List<Author> authors, Genre genre, Publisher publisher,
                  Language language, Short publishedYear, Integer pages, String shelfLocation, String description, String coverUrl) {
        this.isbn13 = isbn13;
        this.title = title;
        this.subtitle = subtitle;
        this.authors.clear();
        this.authors.addAll(authors);
        this.genre = genre;
        this.publisher = publisher;
        this.language = language;
        this.publishedYear = publishedYear;
        this.pages = pages;
        this.shelfLocation = shelfLocation;
        this.description = description;
        this.coverUrl = coverUrl;
    }

    void markDeleted(Instant now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Stock movements. Only the inventory and circulation services call these, and each call is paired with a
    // ledger row in the same transaction.

    public StockChange addCopies(int quantity) {
        requirePositive(quantity);
        return apply(quantity, quantity);
    }

    public StockChange removeCopies(int quantity) {
        requirePositive(quantity);
        if (availableQuantity < quantity) {
            int onLoan = totalQuantity - availableQuantity;
            throw new ApiException(ErrorCode.INSUFFICIENT_STOCK,
                    "Only %d of %d copies of \"%s\" are on the shelf%s. Remove %d or fewer."
                            .formatted(availableQuantity, totalQuantity, title, onLoan > 0 ? " (" + onLoan + " on loan)" : "", availableQuantity),
                    Map.of("available", availableQuantity, "total", totalQuantity));
        }
        return apply(-quantity, -quantity);
    }

    public StockChange checkOut() {
        if (availableQuantity < 1) {
            throw new ApiException(ErrorCode.OUT_OF_STOCK,
                    "All copies of \"%s\" are on loan. Try again when one is returned.".formatted(title));
        }
        return apply(0, -1);
    }

    public StockChange checkIn() {
        if (availableQuantity + 1 > totalQuantity) {
            throw new ApiException(ErrorCode.STOCK_LIMIT_EXCEEDED,
                    "Every copy of \"%s\" is already on the shelf, so this return can't be recorded.".formatted(title));
        }
        return apply(0, 1);
    }

    private StockChange apply(int totalDelta, int availableDelta) {
        int totalBefore = totalQuantity;
        int availableBefore = availableQuantity;
        totalQuantity += totalDelta;
        availableQuantity += availableDelta;
        if (availableQuantity < 0 || totalQuantity < 0) {
            throw new ApiException(ErrorCode.INSUFFICIENT_STOCK);
        }
        if (availableQuantity > totalQuantity) {
            throw new ApiException(ErrorCode.STOCK_LIMIT_EXCEEDED);
        }
        return new StockChange(availableDelta, totalBefore, totalQuantity, availableBefore, availableQuantity);
    }

    private static void requirePositive(int quantity) {
        if (quantity < 1) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Quantity must be at least 1.");
        }
    }

    public String getIsbn13() {
        return isbn13;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public Genre getGenre() {
        return genre;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Language getLanguage() {
        return language;
    }

    public Short getPublishedYear() {
        return publishedYear;
    }

    public Integer getPages() {
        return pages;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public long getVersion() {
        return version;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
