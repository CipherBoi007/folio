package com.folio.circulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.folio.auth.AuthenticatedUser;
import com.folio.auth.Role;
import com.folio.circulation.CirculationDtos.IssueRequest;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

class CirculationIT extends AbstractIntegrationTest {

    @Autowired
    private CirculationService circulation;

    private String desk;

    @BeforeEach
    void signIn() throws Exception {
        desk = deskToken();
    }

    private long createBook(int copies) throws Exception {
        String body = mvc.perform(json(post("/api/v1/books"), desk, """
                        {"isbn":"%s","title":"Circulation Test %s","authors":["Test Author"],"genre":"Testing",
                         "languageCode":"en","initialQuantity":%d}
                        """.formatted(newIsbn(), UUID.randomUUID().toString().substring(0, 6), copies)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createMember() throws Exception {
        String phone = "9" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
        String body = mvc.perform(json(post("/api/v1/members"), desk, """
                        {"fullName":"Test Member","email":"%s","phone":"%s"}
                        """.formatted(uniqueEmail(), phone)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private ResultActions issue(long member, long book) throws Exception {
        return mvc.perform(json(post("/api/v1/loans/issue"), desk, "{\"memberId\":%d,\"bookId\":%d}".formatted(member, book)));
    }

    private long issueOk(long member, long book) throws Exception {
        String body = issue(member, book).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private int available(long book) {
        return jdbc.queryForObject("SELECT available_quantity FROM books WHERE id = ?", Integer.class, book);
    }

    private void travel(Duration duration) throws Exception {
        clock.advance(duration);
        desk = deskToken();
    }

    @Test
    void r5_r6_issueSetsDueDateAndTakesACopyWithLedgerRow() throws Exception {
        long book = createBook(2);
        long member = createMember();
        LocalDate expectedDue = LocalDate.now(clock).plusDays(14);

        String body = issue(member, book)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value(expectedDue.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.renewable").value(true))
                .andReturn().getResponse().getContentAsString();
        long loan = ((Number) JsonPath.read(body, "$.id")).longValue();

        assertThat(available(book)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT type FROM inventory_transactions WHERE loan_id = ?", String.class, loan)).isEqualTo("ISSUE");
    }

    @Test
    void issueRenewReturnHappyPath() throws Exception {
        long book = createBook(1);
        long member = createMember();
        long loan = issueOk(member, book);
        LocalDate due = LocalDate.now(clock).plusDays(14);

        mvc.perform(as(post("/api/v1/loans/" + loan + "/renew"), desk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value(due.plusDays(14).toString()))
                .andExpect(jsonPath("$.renewalCount").value(1))
                .andExpect(jsonPath("$.renewable").value(false));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/renew"), desk))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RENEWAL_LIMIT_REACHED"));

        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loan.status").value("RETURNED"))
                .andExpect(jsonPath("$.fine").doesNotExist());
        assertThat(available(book)).isEqualTo(1);
        assertThat(jdbc.queryForList("SELECT type FROM inventory_transactions WHERE book_id = ? ORDER BY id", String.class, book))
                .containsExactly("INITIAL_STOCK", "ISSUE", "RETURN");

        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_RETURNED"));
    }

    @Test
    void lateReturnCreatesFineAtDailyRate() throws Exception {
        long book = createBook(1);
        long member = createMember();
        long loan = issueOk(member, book);
        travel(Duration.ofDays(14 + 6));

        mvc.perform(as(get("/api/v1/loans/" + loan), desk))
                .andExpect(jsonPath("$.overdue").value(true))
                .andExpect(jsonPath("$.overdueDays").value(6))
                .andExpect(jsonPath("$.projectedFine").value(30.0));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/renew"), desk))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOAN_OVERDUE"));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fine.overdueDays").value(6))
                .andExpect(jsonPath("$.fine.amount").value(30.0))
                .andExpect(jsonPath("$.fine.status").value("UNPAID"));
    }

    @Test
    void r4_memberCannotHoldMoreThanThreeBooks() throws Exception {
        long member = createMember();
        for (int i = 0; i < 3; i++) {
            issueOk(member, createBook(1));
        }
        issue(member, createBook(1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BORROW_LIMIT_REACHED"))
                .andExpect(jsonPath("$.message").value("Test Member already has 3 books on loan. Return one before issuing another."));
        mvc.perform(as(get("/api/v1/members/" + member + "/eligibility"), desk))
                .andExpect(jsonPath("$.canBorrow").value(false))
                .andExpect(jsonPath("$.remainingSlots").value(0));
    }

    @Test
    void sameTitleCannotBeBorrowedTwice() throws Exception {
        long book = createBook(3);
        long member = createMember();
        issueOk(member, book);
        issue(member, book).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_BORROWED"));
    }

    @Test
    void outOfStockBookCannotBeIssued() throws Exception {
        long book = createBook(1);
        issueOk(createMember(), book);
        issue(createMember(), book).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
    }

    @Test
    void inactiveOrSuspendedMemberCannotBorrow() throws Exception {
        long member = createMember();
        mvc.perform(json(patch("/api/v1/members/" + member + "/status"), desk, "{\"status\":\"SUSPENDED\"}")).andExpect(status().isOk());
        issue(member, createBook(1)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER_INACTIVE"));
    }

    @Test
    void overdueLoansBlockNewIssues() throws Exception {
        long member = createMember();
        issueOk(member, createBook(1));
        travel(Duration.ofDays(15));
        issue(member, createBook(1)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("HAS_OVERDUE_LOANS"));
    }

    @Test
    void unpaidFinesAboveThresholdBlockNewIssues() throws Exception {
        long member = createMember();
        long loan = issueOk(member, createBook(1));
        travel(Duration.ofDays(14 + 21));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk)).andExpect(jsonPath("$.fine.amount").value(105.0));

        issue(member, createBook(1)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FINE_LIMIT_EXCEEDED"));

        long fine = jdbc.queryForObject("SELECT id FROM fines WHERE loan_id = ?", Long.class, loan);
        mvc.perform(json(post("/api/v1/fines/" + fine + "/waive"), desk, "{\"reason\":\"Goodwill\"}")).andExpect(status().isForbidden());
        mvc.perform(json(post("/api/v1/fines/" + fine + "/pay"), desk, "{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(json(post("/api/v1/fines/" + fine + "/pay"), desk, "{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FINE_ALREADY_RESOLVED"));
        issueOk(member, createBook(1));
    }

    @Test
    void adminCanWaiveWithReason() throws Exception {
        long member = createMember();
        long loan = issueOk(member, createBook(1));
        travel(Duration.ofDays(16));
        mvc.perform(as(post("/api/v1/loans/" + loan + "/return"), desk)).andExpect(status().isOk());
        long fine = jdbc.queryForObject("SELECT id FROM fines WHERE loan_id = ?", Long.class, loan);

        String admin = adminToken();
        mvc.perform(json(post("/api/v1/fines/" + fine + "/waive"), admin, "{\"reason\":\" \"}")).andExpect(status().isBadRequest());
        mvc.perform(json(post("/api/v1/fines/" + fine + "/waive"), admin, "{\"reason\":\"Library was closed for Pongal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"))
                .andExpect(jsonPath("$.resolutionNote").value("Library was closed for Pongal"));
    }

    @Test
    void unknownMemberOrBookIsNotFound() throws Exception {
        issue(999_999, createBook(1)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"));
        issue(createMember(), 999_999).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    void concurrentIssuesForTheLastCopyAllowExactlyOne() throws Exception {
        long book = createBook(1);
        long first = createMember();
        long second = createMember();
        Long deskId = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, DESK_EMAIL);
        AuthenticatedUser actor = new AuthenticatedUser(deskId, DESK_EMAIL, "Karthik Raja", Role.LIBRARIAN, UUID.randomUUID());

        CountDownLatch start = new CountDownLatch(1);
        List<Callable<String>> attempts = new ArrayList<>();
        for (long member : new long[] {first, second}) {
            attempts.add(() -> {
                start.await();
                try {
                    circulation.issue(new IssueRequest(member, book), actor);
                    return "OK";
                } catch (ApiException e) {
                    return e.code().name();
                }
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (Callable<String> attempt : attempts) {
                futures.add(pool.submit(attempt));
            }
            start.countDown();
            List<String> outcomes = new ArrayList<>();
            for (Future<String> future : futures) {
                outcomes.add(future.get());
            }
            assertThat(outcomes).containsExactlyInAnyOrder("OK", ErrorCode.OUT_OF_STOCK.name());
        } finally {
            pool.shutdownNow();
        }
        assertThat(available(book)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM loans WHERE book_id = ?", Integer.class, book)).isEqualTo(1);
    }

    @Test
    void loanListingFilters() throws Exception {
        mvc.perform(as(get("/api/v1/loans").param("overdue", "true"), desk))
                .andExpect(jsonPath("$.content[0].overdue").value(true));
        mvc.perform(as(get("/api/v1/loans").param("dueToday", "true"), desk))
                .andExpect(jsonPath("$.content[0].dueToday").value(true));
        mvc.perform(as(get("/api/v1/loans").param("status", "ACTIVE").param("q", "priya"), desk))
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
