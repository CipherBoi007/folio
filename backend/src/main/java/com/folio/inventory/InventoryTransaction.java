package com.folio.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "total_before", nullable = false)
    private int totalBefore;

    @Column(name = "total_after", nullable = false)
    private int totalAfter;

    @Column(name = "available_before", nullable = false)
    private int availableBefore;

    @Column(name = "available_after", nullable = false)
    private int availableAfter;

    @Column(length = 40)
    private String reason;

    @Column(length = 500)
    private String note;

    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "performed_by", nullable = false)
    private long performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    protected InventoryTransaction() {
    }

    InventoryTransaction(long bookId, TransactionType type, StockChange change, String reason, String note, Long loanId,
                         long performedBy, Instant performedAt) {
        this.bookId = bookId;
        this.type = type;
        this.quantityChange = change.quantityChange();
        this.totalBefore = change.totalBefore();
        this.totalAfter = change.totalAfter();
        this.availableBefore = change.availableBefore();
        this.availableAfter = change.availableAfter();
        this.reason = reason;
        this.note = note;
        this.loanId = loanId;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public int getQuantityChange() {
        return quantityChange;
    }
}
