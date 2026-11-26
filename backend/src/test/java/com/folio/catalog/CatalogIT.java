package com.folio.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogIT extends AbstractIntegrationTest {

    private String admin;
    private String desk;

    @BeforeEach
    void signIn() throws Exception {
        admin = adminToken();
        desk = deskToken();
    }

    private String bookJson(String isbn, int copies) {
        return """
                {"isbn":"%s","title":"The Test of Time","authors":["Ada Lovelace","Charles Babbage"],"genre":"Technology",
                 "publisher":"Folio Press","languageCode":"en","publishedYear":2020,"pages":200,"shelfLocation":"z-09",
                 "initialQuantity":%d}
                """.formatted(isbn, copies);
    }

    private long createBook(String isbn, int copies) throws Exception {
        String body = mvc.perform(json(post("/api/v1/books"), desk, bookJson(isbn, copies)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private List<Map<String, Object>> ledger(long bookId) {
        return jdbc.queryForList("SELECT type, quantity_change, total_after, available_after FROM inventory_transactions WHERE book_id = ? ORDER BY id", bookId);
    }

    @Test
    void createNormalisesIsbn10AndLogsInitialStock() throws Exception {
        String isbn13 = newIsbn();
        long id = createBook(isbn13, 3);

        mvc.perform(as(get("/api/v1/books/" + id), desk))
                .andExpect(jsonPath("$.isbn13").value(isbn13))
                .andExpect(jsonPath("$.authors[0]").value("Ada Lovelace"))
                .andExpect(jsonPath("$.shelfLocation").value("Z-09"))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.availableQuantity").value(3));
        assertThat(ledger(id)).singleElement().satisfies(row -> {
            assertThat(row.get("type")).isEqualTo("INITIAL_STOCK");
            assertThat(row.get("quantity_change")).isEqualTo(3);
        });
        Integer activity = jdbc.queryForObject("SELECT count(*) FROM activity_log WHERE entity_type = 'BOOK' AND entity_id = ? AND action = 'BOOK_CREATED'",
                Integer.class, id);
        assertThat(activity).isEqualTo(1);
    }

    @Test
    void isbn10IsStoredAsIsbn13() throws Exception {
        mvc.perform(json(post("/api/v1/books"), desk, bookJson("0-8044-2957-X", 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn13").value("9780804429573"));
    }

    @Test
    void r1_duplicateAndInvalidIsbnsAreRejected() throws Exception {
        String isbn = newIsbn();
        createBook(isbn, 1);
        mvc.perform(json(post("/api/v1/books"), desk, bookJson(isbn.substring(0, 3) + "-" + isbn.substring(3), 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_ISBN"));
        mvc.perform(json(post("/api/v1/books"), desk, bookJson("9780143039656", 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ISBN"));
    }

    @Test
    void updateChangesMetadataButNeverQuantities() throws Exception {
        long id = createBook(newIsbn(), 2);
        String isbn = jdbc.queryForObject("SELECT isbn13 FROM books WHERE id = ?", String.class, id);
        String body = bookJson(isbn, 99).replace("The Test of Time", "The Test of Time, Revised");

        mvc.perform(json(put("/api/v1/books/" + id), desk, body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Test of Time, Revised"))
                .andExpect(jsonPath("$.totalQuantity").value(2));
        assertThat(ledger(id)).hasSize(1);
    }

    @Test
    void stockInAndOutAreLedgeredAndBounded() throws Exception {
        long id = createBook(newIsbn(), 2);

        mvc.perform(json(post("/api/v1/books/" + id + "/stock/in"), desk, "{\"quantity\":3,\"reason\":\"Purchase\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(5))
                .andExpect(jsonPath("$.availableQuantity").value(5));
        mvc.perform(json(post("/api/v1/books/" + id + "/stock/out"), desk, "{\"quantity\":1,\"reason\":\"DAMAGED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(post("/api/v1/books/" + id + "/stock/out"), admin, "{\"quantity\":6,\"reason\":\"LOST\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        mvc.perform(json(post("/api/v1/books/" + id + "/stock/out"), admin, "{\"quantity\":2,\"reason\":\"DAMAGED\",\"note\":\"Spine broken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3));
        mvc.perform(json(post("/api/v1/books/" + id + "/stock/in"), desk, "{\"quantity\":0,\"reason\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("quantity")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("reason")));

        assertThat(ledger(id)).extracting(r -> r.get("type")).containsExactly("INITIAL_STOCK", "STOCK_IN", "STOCK_OUT");
        mvc.perform(as(get("/api/v1/books/" + id + "/transactions"), desk))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].type").value("STOCK_OUT"))
                .andExpect(jsonPath("$.content[0].note").value("Spine broken"));
    }

    @Test
    void deleteIsAdminOnlySoftAndKeepsIsbnReserved() throws Exception {
        String isbn = newIsbn();
        long id = createBook(isbn, 1);

        mvc.perform(as(delete("/api/v1/books/" + id), desk)).andExpect(status().isForbidden());
        mvc.perform(as(delete("/api/v1/books/" + id), admin)).andExpect(status().isNoContent());
        mvc.perform(as(get("/api/v1/books/" + id), desk)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
        mvc.perform(json(post("/api/v1/books"), desk, bookJson(isbn, 1))).andExpect(jsonPath("$.code").value("DUPLICATE_ISBN"));
    }

    @Test
    void deleteIsBlockedWhileCopiesAreOnLoan() throws Exception {
        Long onLoan = jdbc.queryForObject("SELECT min(book_id) FROM loans WHERE returned_at IS NULL", Long.class);
        mvc.perform(as(delete("/api/v1/books/" + onLoan), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_HAS_ACTIVE_LOANS"));
    }

    @Test
    void searchFilterAndSort() throws Exception {
        mvc.perform(as(get("/api/v1/books").param("q", "narayan"), desk))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].authors[0]", everyItem(org.hamcrest.Matchers.is("R. K. Narayan"))));
        mvc.perform(as(get("/api/v1/books").param("q", "978-0-14-303965"), desk))
                .andExpect(jsonPath("$.content[0].title").value("Malgudi Days"));
        mvc.perform(as(get("/api/v1/books").param("availability", "OUT"), desk))
                .andExpect(jsonPath("$.content[*].availableQuantity", everyItem(org.hamcrest.Matchers.is(0))));
        mvc.perform(as(get("/api/v1/books").param("language", "ta"), desk))
                .andExpect(jsonPath("$.content[0].title").value("Ponniyin Selvan"));
        mvc.perform(as(get("/api/v1/books").param("sort", "available,desc").param("size", "5"), desk))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content[0].availableQuantity", greaterThan(2)));
        mvc.perform(as(get("/api/v1/books/genres"), desk)).andExpect(jsonPath("$[*].name", hasItem("Classics")));
    }

    @Test
    void csvExportStreamsTheCatalogue() throws Exception {
        mvc.perform(as(get("/api/v1/books/export.csv").param("q", "Malgudi"), desk))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("9780143039655,Malgudi Days")));
    }
}
