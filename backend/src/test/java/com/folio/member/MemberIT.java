package com.folio.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberIT extends AbstractIntegrationTest {

    private String desk;

    @BeforeEach
    void signIn() throws Exception {
        desk = deskToken();
    }

    private String memberJson(String email, String phone) {
        return """
                {"fullName":"  Thenmozhi   Arasu ","email":"%s","phone":"%s","addressLine":"5 Temple Street",
                 "city":"Madurai","postalCode":"625001"}
                """.formatted(email, phone);
    }

    @Test
    void registerAssignsMembershipNumberAndNormalisesInput() throws Exception {
        String email = uniqueEmail();
        mvc.perform(json(post("/api/v1/members"), desk, memberJson(email.toUpperCase(), "+91 98765-43210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipNumber", Matchers.matchesPattern("FOL-\\d{4}-\\d{4,}")))
                .andExpect(jsonPath("$.fullName").value("Thenmozhi Arasu"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void duplicateEmailAndInvalidPhoneAreFieldErrors() throws Exception {
        mvc.perform(json(post("/api/v1/members"), desk, memberJson("Priya.Raman@example.com", "9876543210")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        mvc.perform(json(post("/api/v1/members"), desk, memberJson(uniqueEmail(), "0442345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("phone"));
    }

    @Test
    void cannotDeactivateOrDeleteWithBooksOnLoan() throws Exception {
        Long member = jdbc.queryForObject("SELECT min(member_id) FROM loans WHERE returned_at IS NULL", Long.class);
        mvc.perform(json(patch("/api/v1/members/" + member + "/status"), desk, "{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_HAS_ACTIVE_LOANS"));
        mvc.perform(as(delete("/api/v1/members/" + member), adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_HAS_ACTIVE_LOANS"));
    }

    @Test
    void statusChangesAndSoftDelete() throws Exception {
        String body = mvc.perform(json(post("/api/v1/members"), desk, memberJson(uniqueEmail(), "9123456789")))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(json(patch("/api/v1/members/" + id + "/status"), desk, "{\"status\":\"SUSPENDED\"}"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
        mvc.perform(as(get("/api/v1/members/" + id + "/eligibility"), desk))
                .andExpect(jsonPath("$.canBorrow").value(false))
                .andExpect(jsonPath("$.reasons[0].code").value("MEMBER_INACTIVE"));
        mvc.perform(as(delete("/api/v1/members/" + id), desk)).andExpect(status().isForbidden());
        mvc.perform(as(delete("/api/v1/members/" + id), adminToken())).andExpect(status().isNoContent());
        mvc.perform(as(get("/api/v1/members/" + id), desk)).andExpect(status().isNotFound());
    }

    @Test
    void searchByNamePhoneOrMembershipNumber() throws Exception {
        mvc.perform(as(get("/api/v1/members").param("q", "priya"), desk))
                .andExpect(jsonPath("$.content[0].fullName").value("Priya Raman"))
                .andExpect(jsonPath("$.content[0].activeLoans").value(3));
        mvc.perform(as(get("/api/v1/members").param("q", "98430 12345"), desk))
                .andExpect(jsonPath("$.content[0].fullName").value("Priya Raman"));
        mvc.perform(as(get("/api/v1/members").param("status", "SUSPENDED"), desk))
                .andExpect(jsonPath("$.content[0].fullName").value("Rajesh Kannan"));
    }
}
