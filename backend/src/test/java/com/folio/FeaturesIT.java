package com.folio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FeaturesIT extends AbstractIntegrationTest {

    private long id(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    @Test
    void waitlistBlocksRenewalAndShowsNextInQueue() throws Exception {
        String desk = deskToken();
        long book = id("SELECT id FROM books WHERE isbn13 = '9780618329960'");
        long loan = id("SELECT id FROM loans WHERE book_id = ? AND returned_at IS NULL", book);
        long waiting = id("SELECT id FROM members WHERE email = 'divya.bala@example.com'");

        mvc.perform(json(post("/api/v1/books/" + book + "/waitlist"), desk, "{\"memberId\":" + waiting + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.position").value(1));
        mvc.perform(json(post("/api/v1/books/" + book + "/waitlist"), desk, "{\"memberId\":" + waiting + "}"))
                .andExpect(jsonPath("$.code").value("ALREADY_WAITLISTED"));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/renew"), desk))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_RESERVED"));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk))
                .andExpect(jsonPath("$.nextInQueue.id").value(waiting));
        mvc.perform(json(post("/api/v1/books/" + book + "/waitlist"), desk, "{\"memberId\":" + waiting + "}"))
                .andExpect(jsonPath("$.code").value("BOOK_AVAILABLE"));
    }

    @Test
    void csvImportReportsEachRow() throws Exception {
        String csv = """
                isbn,title,authors,genre,language,publisher,year,pages,shelf,copies
                %s,"Imported, With Comma",Author One;Author Two,Poetry,Tamil,Folio Press,2020,120,P-01,2
                9780143039656,Bad Checksum,Someone,Poetry,en,,,,,1
                9780143039655,Malgudi Days,R. K. Narayan,Fiction,English,,,,,1
                """.formatted(newIsbn());
        MockMultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/v1/books/import").file(file).param("dryRun", "true").header("Authorization", "Bearer " + deskToken()))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.results[0].status").value("READY"));
        mvc.perform(multipart("/api/v1/books/import").file(file).header("Authorization", "Bearer " + deskToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.failed").value(2))
                .andExpect(jsonPath("$.results[1].message").value("That ISBN is not valid. Check the digits and try again."))
                .andExpect(jsonPath("$.results[2].status").value("FAILED"));
    }

    @Test
    void adminManagesStaffAndPasswordResetForcesChange() throws Exception {
        String admin = adminToken();
        String body = mvc.perform(json(post("/api/v1/staff"), admin,
                        "{\"name\":\"Anbu Selvan\",\"email\":\"anbu@folio.dev\",\"role\":\"LIBRARIAN\",\"temporaryPassword\":\"Welcome123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mustChangePassword").value(true))
                .andReturn().getResponse().getContentAsString();
        long staffId = ((Number) JsonPath.read(body, "$.id")).longValue();

        String token = login("anbu@folio.dev", "Welcome123");
        mvc.perform(json(post("/api/v1/auth/change-password"), token, "{\"currentPassword\":\"nope\",\"newPassword\":\"Better456\"}"))
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
        mvc.perform(json(post("/api/v1/auth/change-password"), token, "{\"currentPassword\":\"Welcome123\",\"newPassword\":\"Better456\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(json(post("/api/v1/staff/" + staffId + "/reset-password"), deskToken(), "{\"temporaryPassword\":\"Reset789x\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(json(post("/api/v1/staff/" + staffId + "/reset-password"), admin, "{\"temporaryPassword\":\"Reset789x\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(as(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/auth/me"), token))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"anbu@folio.dev\",\"password\":\"Reset789x\"}"))
                .andExpect(jsonPath("$.user.mustChangePassword").value(true));

        long adminId = id("SELECT id FROM users WHERE email = ?", ADMIN_EMAIL);
        mvc.perform(json(put("/api/v1/staff/" + adminId), admin, "{\"name\":\"Meenakshi Sundaram\",\"role\":\"LIBRARIAN\",\"active\":true}"))
                .andExpect(jsonPath("$.code").value("SELF_MODIFICATION"));
    }
}
