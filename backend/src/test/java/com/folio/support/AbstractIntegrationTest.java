package com.folio.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration tests run against real PostgreSQL: a Testcontainers instance, or the database in {@code TEST_DB_URL}
 * when Docker is unavailable. Flyway cleans and migrates once per Spring context, then the demo library is loaded.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIntegrationTest.TestConfig.class)
public abstract class AbstractIntegrationTest {

    public static final String ADMIN_EMAIL = "admin@folio.dev";
    public static final String ADMIN_PASSWORD = "Admin@123";
    public static final String DESK_EMAIL = "desk@folio.dev";
    public static final String DESK_PASSWORD = "Desk@123";
    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private static final String EXTERNAL_DB_URL = System.getenv("TEST_DB_URL");
    private static final PostgreSQLContainer POSTGRES = startContainerIfNeeded();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected MutableClock clock;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void resetClock() {
        clock.set(Instant.now());
    }

    private static PostgreSQLContainer startContainerIfNeeded() {
        if (EXTERNAL_DB_URL != null && !EXTERNAL_DB_URL.isBlank()) {
            return null;
        }
        PostgreSQLContainer container = new PostgreSQLContainer("postgres:16-alpine");
        container.start();
        return container;
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> EXTERNAL_DB_URL);
            registry.add("spring.datasource.username", () -> envOr("TEST_DB_USERNAME", "postgres"));
            registry.add("spring.datasource.password", () -> envOr("TEST_DB_PASSWORD", "postgres"));
        }
    }

    protected MvcResult loginResult(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    protected String login(String email, String password) throws Exception {
        return JsonPath.read(loginResult(email, password).getResponse().getContentAsString(), "$.accessToken");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected String deskToken() throws Exception {
        return login(DESK_EMAIL, DESK_PASSWORD);
    }

    protected static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String token, String body) {
        return as(request, token).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    /** A staff account private to one test (password Desk@123), so lockout tests don't affect shared users. */
    protected String createStaff(String role) {
        String email = "staff-" + UUID.randomUUID().toString().substring(0, 8) + "@folio.dev";
        jdbc.update("""
                INSERT INTO users (name, email, password_hash, role)
                SELECT 'Test Staff', ?, password_hash, ? FROM users WHERE email = ?
                """, email, role, DESK_EMAIL);
        return email;
    }

    /** A valid ISBN-13 that is not in the demo catalogue. */
    protected static String newIsbn() {
        String core = "979" + String.format("%09d", Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 1_000_000_000L));
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = core.charAt(i) - '0';
            sum += i % 2 == 0 ? digit : digit * 3;
        }
        return core + (10 - sum % 10) % 10;
    }

    protected static String uniqueEmail() {
        return "member-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        FlywayMigrationStrategy cleanMigrate() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }

        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock(Instant.now(), ZONE);
        }
    }
}
