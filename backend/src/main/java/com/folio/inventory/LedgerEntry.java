package com.folio.inventory;

import java.time.Instant;

public record LedgerEntry(
        long id,
        BookRef book,
        TransactionType type,
        int quantityChange,
        int totalBefore,
        int totalAfter,
        int availableBefore,
        int availableAfter,
        String reason,
        String note,
        Long loanId,
        ActorRef performedBy,
        Instant performedAt) {

    public record BookRef(long id, String title, String isbn13) {
    }

    public record ActorRef(long id, String name) {
    }
}
