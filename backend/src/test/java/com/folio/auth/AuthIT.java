package com.folio.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthIT extends AbstractIntegrationTest {

    private static final String COOKIE = "folio_rt";

    private static String credentials(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private MvcResult refresh(Cookie cookie) throws Exception {
        return mvc.perform(post("/api/v1/auth/refresh").cookie(cookie)).andReturn();
    }

    @Test
    void loginReturnsAccessTokenAndSetsHardenedRefreshCookie() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andReturn();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains(COOKIE + "=", "HttpOnly", "SameSite=Strict", "Path=/api/v1/auth");
        String raw = result.getResponse().getCookie(COOKIE).getValue();
        Integer stored = jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE token_hash = encode(sha256(?::bytea), 'hex')",
                Integer.class, raw);
        assertThat(stored).as("only the hash is stored").isEqualTo(1);
    }

    @Test
    void refreshRotatesTheCookieAndIssuesNewAccessToken() throws Exception {
        Cookie first = loginResult(DESK_EMAIL, DESK_PASSWORD).getResponse().getCookie(COOKIE);

        MvcResult rotated = refresh(first);
        assertThat(rotated.getResponse().getStatus()).isEqualTo(200);
        Cookie second = rotated.getResponse().getCookie(COOKIE);
        assertThat(second.getValue()).isNotEqualTo(first.getValue());

        String token = JsonPath.read(rotated.getResponse().getContentAsString(), "$.accessToken");
        mvc.perform(as(get("/api/v1/auth/me"), token)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(DESK_EMAIL));
    }

    @Test
    void replayInsideGraceWindowIsToleratedWithoutNewCookie() throws Exception {
        Cookie first = loginResult(DESK_EMAIL, DESK_PASSWORD).getResponse().getCookie(COOKIE);
        refresh(first);

        MvcResult replay = refresh(first);
        assertThat(replay.getResponse().getStatus()).isEqualTo(200);
        assertThat(replay.getResponse().getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void replayAfterGraceWindowRevokesTheWholeSession() throws Exception {
        Cookie first = loginResult(DESK_EMAIL, DESK_PASSWORD).getResponse().getCookie(COOKIE);
        Cookie second = refresh(first).getResponse().getCookie(COOKIE);
        clock.advance(Duration.ofMinutes(2));

        MvcResult stolen = refresh(first);
        assertThat(stolen.getResponse().getStatus()).isEqualTo(401);
        assertThat(JsonPath.<String>read(stolen.getResponse().getContentAsString(), "$.code")).isEqualTo("SESSION_EXPIRED");
        assertThat(stolen.getResponse().getHeader(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");

        assertThat(refresh(second).getResponse().getStatus()).as("legitimate holder is signed out too").isEqualTo(401);
    }

    @Test
    void logoutRevokesTheSessionImmediately() throws Exception {
        MvcResult login = loginResult(DESK_EMAIL, DESK_PASSWORD);
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        Cookie cookie = login.getResponse().getCookie(COOKIE);

        mvc.perform(as(post("/api/v1/auth/logout"), token).cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        mvc.perform(as(get("/api/v1/auth/me"), token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESSION_EXPIRED"));
        assertThat(refresh(cookie).getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void expiredAccessTokenIsDistinguishable() throws Exception {
        String token = deskToken();
        clock.advance(Duration.ofMinutes(16));

        mvc.perform(as(get("/api/v1/auth/me"), token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
    }

    @Test
    void refreshWithoutCookieOrFromForeignOriginIsRejected() throws Exception {
        mvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SESSION_EXPIRED"));

        Cookie cookie = loginResult(DESK_EMAIL, DESK_PASSWORD).getResponse().getCookie(COOKIE);
        mvc.perform(post("/api/v1/auth/refresh").cookie(cookie).header(HttpHeaders.ORIGIN, "https://evil.example"))
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongPasswordAndUnknownEmailLookTheSame() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials("nobody@folio.dev", "x")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Email or password is incorrect."));
    }

    @Test
    void accountLocksAfterRepeatedFailuresAndUnlocksLater() throws Exception {
        String email = createStaff("LIBRARIAN");
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, "wrong")))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, DESK_PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));

        clock.advance(Duration.ofMinutes(16));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, DESK_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void emailIsTrimmedButPasswordIsNot() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials("  Desk@Folio.dev ", DESK_PASSWORD)))
                .andExpect(status().isOk());
        String email = createStaff("LIBRARIAN");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, " " + DESK_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidBodyReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("password")));
    }

    @Test
    void sessionsCanBeListedAndRevoked() throws Exception {
        String email = createStaff("LIBRARIAN");
        String first = login(email, DESK_PASSWORD);
        String second = login(email, DESK_PASSWORD);

        String body = mvc.perform(as(get("/api/v1/auth/sessions"), second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        String other = JsonPath.<java.util.List<String>>read(body, "$[?(@.current == false)].id").getFirst();

        mvc.perform(as(delete("/api/v1/auth/sessions/" + other), second)).andExpect(status().isNoContent());
        mvc.perform(as(get("/api/v1/auth/me"), first)).andExpect(status().isUnauthorized());
        mvc.perform(as(get("/api/v1/auth/me"), second)).andExpect(status().isOk());
    }

    @Test
    void disabledAccountLosesAccessImmediately() throws Exception {
        String email = createStaff("LIBRARIAN");
        String token = login(email, DESK_PASSWORD);
        jdbc.update("UPDATE users SET active = false WHERE email = ?", email);

        mvc.perform(as(get("/api/v1/books"), token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email, DESK_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }
}
