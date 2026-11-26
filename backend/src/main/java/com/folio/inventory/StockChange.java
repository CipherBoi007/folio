package com.folio.inventory;

/** Before/after snapshot of a single stock movement, written verbatim to the inventory ledger. */
public record StockChange(int quantityChange, int totalBefore, int totalAfter, int availableBefore, int availableAfter) {
}
