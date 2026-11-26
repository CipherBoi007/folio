# Folio API

Base path: `/api/v1`. Interactive docs: **`/swagger-ui.html`** (click *Authorize* and paste a token from `POST /auth/login`).
Every endpoint except login needs `Authorization: Bearer <token>`. `/actuator/health` is public.

## Conventions

- **Paged responses:** `{ content, page, size, totalElements, totalPages }`. Query parameters are `page` (0-based), `size` (1–100, default 20) and `sort=field,asc|desc`. Each endpoint allows only its own list of sort fields.
- **Errors:** RFC 7807 `application/problem+json`, for example:

```json
{
  "type": "urn:folio:problem:borrow-limit-reached",
  "title": "Conflict",
  "status": 409,
  "detail": "Priya Raman already has 3 books on loan. Return one before issuing another.",
  "instance": "/api/v1/loans/issue",
  "code": "BORROW_LIMIT_REACHED",
  "message": "Priya Raman already has 3 books on loan. Return one before issuing another.",
  "fieldErrors": [],
  "timestamp": "2026-11-26T05:14:22Z",
  "requestId": "e847c344-cdcf-4ec0-8339-94eb9d72af93"
}
```

  `message` is written for people and the UI shows it as-is. `code` is stable for programs. The full list is in `ErrorCode.java`.
- **Correlation:** send `X-Request-Id` (8–64 characters from `[A-Za-z0-9._-]`) or the server generates one. It is echoed on every response and in every error.

## Endpoints

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login` · `POST /auth/refresh` (cookie) · `POST /auth/logout` · `GET /auth/me` · `GET /auth/sessions` · `DELETE /auth/sessions/{id}` |
| Books | `GET/POST /books` · `GET/PUT/DELETE /books/{id}` · `GET /books/genres` · `GET /books/languages` · `GET /books/lookup?isbn=` · `GET /books/export.csv` |
| Stock | `POST /books/{id}/stock/in` · `POST /books/{id}/stock/out` (admin) · `GET /books/{id}/transactions` · `GET /inventory/transactions` · `GET /inventory/transactions/export.csv` |
| Members | `GET/POST /members` · `GET/PUT/DELETE /members/{id}` · `PATCH /members/{id}/status` · `GET /members/{id}/eligibility` · `GET /members/{id}/loans` · `GET /members/export.csv` |
| Circulation | `POST /loans/issue` · `POST /loans/{id}/return` · `POST /loans/{id}/renew` · `GET /loans` · `GET /loans/{id}` · `GET /loans/export.csv` · `GET /books/{id}/loans` |
| Fines | `GET /fines` · `GET /fines/summary` · `POST /fines/{id}/pay` · `POST /fines/{id}/waive` (admin) |
| Other | `GET /dashboard/summary` · `/loan-trend` · `/top-books` · `/genre-distribution` · `GET /activity` · `GET /search?q=` · `GET /staff` · `GET/PUT /settings` |
