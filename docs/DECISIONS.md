# Decisions and assumptions

Where the brief or CLAUDE.md left room for interpretation, we chose the safest option and wrote it down here.
Newest entries are added at the bottom of each section.

## Platform

| # | Decision | Why |
|---|---|---|
| D1 | **Spring Boot 4.2 / Java 21, Next.js 16.4, Tailwind 4, shadcn (radix base)**: the latest stable versions from the official generators at build time. | CLAUDE.md §2 says not to pin versions from memory. Boot 4 brings Jackson 3 (`tools.jackson.*`) and Spring Security 7. |
| D2 | Local development uses the developer's **existing PostgreSQL 17** (database `folio-db`, user `postgres`). CI, docker-compose and Testcontainers use **PostgreSQL 16**. | That's what is installed locally. The schema uses nothing that differs between 16 and 17. Every local value can be overridden with `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`. |
| D3 | Integration tests use **Testcontainers** (`postgres:16-alpine`) when Docker is available. If `TEST_DB_URL` is set (CI, or no Docker) they use that database instead. Flyway runs `clean` + `migrate` before each Spring test context. | Tests must use real PostgreSQL, never H2: CHECKs, partial indexes, triggers and `FOR UPDATE` must behave exactly as in production. Both paths are verified. |
| D4 | Unit tests are named `*Test` and run in `mvn test` (Surefire). Integration tests are named `*IT` and run in `mvn verify` (Failsafe). | Gives a fast feedback loop plus a full gate, using standard Maven conventions. |
| D5 | Migrations are split by purpose: `V1__schema.sql`, `V2__staff_users.sql`, and later seed migrations get their own versions. | An applied Flyway migration can't be edited without breaking checksums, so the demo seed gets a new version rather than rewriting V2. |
| D6 | The frontend keeps the generator's **Cache Components + Partial Prefetching** settings. The console is client-rendered behind an auth guard, and the server prerenders a static shell. | The generator turned them on, and they're the framework's direction. Auth lives in the browser (see D9), so pages can't render user data on the server anyway. |

## Security

| # | Decision | Why |
|---|---|---|
| D7 | **Access tokens are 15-minute JWTs kept in memory only. Refresh tokens are 256-bit random values stored as SHA-256 hashes in `refresh_tokens`, grouped under `user_sessions`.** Each refresh rotates the token. Replaying a rotated token after a 30 s grace window revokes the whole session. | No token in localStorage means nothing for XSS to steal. Database-backed sessions allow instant sign-out, "sign out other devices" and theft detection. The grace window absorbs two tabs refreshing at once. |
| D8 | The JWT secret must be at least 32 characters, validated at startup. Production has no default. | Fail fast rather than run with a weak secret. |
| D9 | The refresh token travels in an `HttpOnly; Secure; SameSite=Strict` cookie scoped to `/api/v1/auth`. The web app proxies `/api/*` to the API, so the cookie is first-party. Cookie endpoints also verify `Origin`. | Avoids third-party-cookie blocking and CSRF without extra infrastructure. |
| D10 | Failed sign-ins are counted on the user row. 5 failures lock the account for 15 minutes. Every request also checks that its session is live and the user active. | The lockout survives restarts and works across instances. Deactivation takes effect immediately. |
| D11 | Unknown emails and wrong passwords get the **same message and similar timing**. Disabled accounts get the same generic error too. | Prevents account enumeration. |
| D12 | **All JSON string inputs are trimmed** server-side before validation. Passwords are exempt. | CLAUDE.md §4 says "trim inputs". Doing it once in the JSON layer is simpler and more reliable than per-field code. |
| D13 | API errors are RFC 7807 `ProblemDetail` with `type = urn:folio:problem:<code>`. Database constraint names map to the same error codes the services use. | When the database rejects a write the services missed (for example under a race), the client still gets a meaningful code and message. |
| D14 | `X-Request-Id`: an inbound value is reused only if it matches `[A-Za-z0-9._-]{8,64}`; otherwise a UUID is generated. | Correlates logs with client reports and prevents log injection. |

## Data

| # | Decision | Why |
|---|---|---|
| D15 | `inventory_transactions` and `activity_log` are **append-only, enforced by database triggers** that reject UPDATE and DELETE. | Audit history must not be rewritable, even by buggy code or manual SQL. |
| D16 | The ledger has **arithmetic CHECKs**: `available_after − available_before = quantity_change`; totals move with the change except for ISSUE/RETURN; every ISSUE/RETURN row must reference a loan. | Satisfies R7 ("every stock change logged") structurally: a row whose numbers don't add up cannot be stored. |
| D17 | The ISBN `UNIQUE` constraint is **not partial**: a soft-deleted book keeps its ISBN reserved. | As specified (§4 › delete book). Re-adding a title means restoring the record, which preserves its history. |
| D18 | Schema in normal form: genres, publishers, authors (many-to-many with order) and languages are reference tables. Loan status is derived from `returned_at`. A fine stores days and daily rate, with `amount` a generated column. Fines reach the member through the loan, so there is no duplicated `member_id`. Book quantities are a deliberate cached aggregate of the ledger, guarded by CHECKs and a consistency test. | Removes update anomalies while keeping hot reads cheap. |
| D18b | Member email is **globally unique, including soft-deleted members**. | The safest option: no two records ever share an identity. A returning member is reactivated rather than registered again. |
| D19 | The renewal limit (one renewal) is also a DB `CHECK (renewal_count BETWEEN 0 AND 1)`. The 3-book borrowing limit is enforced only in the service, under a pessimistic lock on the member row. | The renewal limit is fixed, so the database can enforce it. The borrowing limit is configurable (Tier 3 settings), and a row-count rule would need a trigger that could drift from the setting. |

## UI

| # | Decision | Why |
|---|---|---|
| D20 | The design system's radius scale is **clamped at 8px**, and its shadow scale is **flat except for overlays**. | This makes the banned looks (`rounded-2xl` cards, large drop shadows) impossible even if a generated component asks for them. |
| D21 | Generic shadcn styling was removed from every primitive: backdrop blur, zoom animations, ring outlines and tinted footer bands. Overlays fade and slide by 4px over 150–200ms. | CLAUDE.md §8: never ship the default shadcn look. |
| D22 | The top bar's primary action is always **Issue book**. Page-specific actions sit in the page header. | Issuing is the desk's main job and should be one click from anywhere. |
| D23 | The demo-login buttons sit on the login page and are labelled with their role and scope. | The brief asks for the app to be easy to evaluate. The credentials are also in the README. |
