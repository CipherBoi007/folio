package com.folio.common;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class PlatformIT extends AbstractIntegrationTest {

    @Test
    void healthAndApiDocsArePublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void everyApiEndpointRequiresAToken() throws Exception {
        for (String path : new String[] {"/api/v1/books", "/api/v1/members", "/api/v1/loans", "/api/v1/fines",
                "/api/v1/inventory/transactions", "/api/v1/activity", "/api/v1/dashboard/summary", "/api/v1/settings"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
    }

    @Test
    void requestIdIsGeneratedEchoedAndSanitised() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(header().string("X-Request-Id", notNullValue()));
        mvc.perform(get("/api/v1/auth/me").header("X-Request-Id", "req-1234abcd"))
                .andExpect(header().string("X-Request-Id", "req-1234abcd"))
                .andExpect(jsonPath("$.requestId").value("req-1234abcd"));
        mvc.perform(get("/actuator/health").header("X-Request-Id", "<script>"))
                .andExpect(header().string("X-Request-Id", not("<script>")));
    }

    @Test
    void unknownRouteReturnsProblemDetailWithoutInternals() throws Exception {
        mvc.perform(as(get("/api/v1/does-not-exist"), deskToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.type").value("urn:folio:problem:not-found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/does-not-exist"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void invalidSortAndPageSizeAreRejected() throws Exception {
        String token = deskToken();
        mvc.perform(as(get("/api/v1/books").param("sort", "password_hash"), token))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_SORT"));
        mvc.perform(as(get("/api/v1/books").param("size", "500"), token))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void corsAllowsOnlyConfiguredOrigins() throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));
        mvc.perform(options("/api/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianCannotChangeLibraryRules() throws Exception {
        String body = "{\"loanDays\":14,\"renewalDays\":14,\"maxActiveLoans\":3,\"finePerDay\":5,\"fineBlockThreshold\":100}";
        mvc.perform(json(put("/api/v1/settings"), deskToken(), body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(json(put("/api/v1/settings"), adminToken(), body))
                .andExpect(status().isOk());
    }
}
