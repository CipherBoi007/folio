package com.folio.inventory;

import com.folio.common.csv.CsvWriter;
import com.folio.common.web.PageResponse;
import com.folio.common.web.Pagination;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryLedger ledger;

    public InventoryController(InventoryLedger ledger) {
        this.ledger = ledger;
    }

    @GetMapping("/inventory/transactions")
    public PageResponse<LedgerEntry> list(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = Pagination.pageable(page, size, null, Map.of(), Sort.unsorted());
        return ledger.search(new InventoryLedger.LedgerFilter(bookId, type, actorId, from, to), pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("/books/{bookId}/transactions")
    public PageResponse<LedgerEntry> forBook(@PathVariable long bookId,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size) {
        Pageable pageable = Pagination.pageable(page, size, null, Map.of(), Sort.unsorted());
        return ledger.search(new InventoryLedger.LedgerFilter(bookId, null, null, null, null), pageable.getPageNumber(), pageable.getPageSize());
    }

    @GetMapping("/inventory/transactions/export.csv")
    public void export(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            HttpServletResponse response) throws IOException {
        CsvWriter csv = CsvWriter.attachment(response, "folio-inventory-transactions.csv")
                .row("Date", "Type", "Book", "ISBN", "Change", "Total before", "Total after", "Available before",
                        "Available after", "Reason", "Note", "Loan", "Performed by");
        ledger.stream(new InventoryLedger.LedgerFilter(bookId, type, actorId, from, to), e -> csv.row(
                e.performedAt(), e.type(), e.book().title(), e.book().isbn13(), e.quantityChange(), e.totalBefore(),
                e.totalAfter(), e.availableBefore(), e.availableAfter(), e.reason(), e.note(), e.loanId(), e.performedBy().name()));
        csv.flush();
    }
}
