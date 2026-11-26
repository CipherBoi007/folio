package com.folio.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.inventory.StockChange;
import java.util.List;
import org.junit.jupiter.api.Test;

/** R2 (available ≤ total), R3 (no negative stock) and R6 (issue/return move availability) at the entity level. */
class BookStockTest {

    private static Book book(int copies) {
        Book book = new Book("9780143039655");
        book.describe("9780143039655", "Malgudi Days", null, List.of(), null, null, null, null, null, null, null, null);
        if (copies > 0) {
            book.addCopies(copies);
        }
        return book;
    }

    @Test
    void addCopiesRaisesTotalAndAvailable() {
        Book book = book(2);
        StockChange change = book.addCopies(3);

        assertThat(change).isEqualTo(new StockChange(3, 2, 5, 2, 5));
        assertThat(book.getTotalQuantity()).isEqualTo(5);
        assertThat(book.getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    void removeCopiesCannotTouchCopiesOnLoan() {
        Book book = book(3);
        book.checkOut();

        assertThatThrownBy(() -> book.removeCopies(3))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.code()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
                    assertThat(e.getMessage()).contains("Only 2 of 3", "1 on loan");
                });
        assertThat(book.removeCopies(2)).isEqualTo(new StockChange(-2, 3, 1, 2, 0));
    }

    @Test
    void quantitiesMustBePositive() {
        Book book = book(1);
        assertThatThrownBy(() -> book.addCopies(0)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> book.removeCopies(-1)).isInstanceOf(ApiException.class);
    }

    @Test
    void checkOutAndCheckInMoveOnlyAvailability() {
        Book book = book(1);

        assertThat(book.checkOut()).isEqualTo(new StockChange(-1, 1, 1, 1, 0));
        assertThatThrownBy(book::checkOut)
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.OUT_OF_STOCK));
        assertThat(book.checkIn()).isEqualTo(new StockChange(1, 1, 1, 0, 1));
        assertThatThrownBy(book::checkIn)
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.STOCK_LIMIT_EXCEEDED));
        assertThat(book.getTotalQuantity()).isEqualTo(1);
    }
}
