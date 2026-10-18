# Known issues and triage

## Application / published-contract defects retained in regression

| ID | Trigger | Expected contract | Observed | Classification |
|---|---|---|---|---|
| AUT-01 | GET `/api/room/2147483647` (nonexistent numeric ID) | 404 in RoomService | 500, Spring Internal Server Error | Application defect; source DB query appears to read an empty ResultSet without checking existence; precise deployed cause unconfirmed |
| AUT-02 | POST `/api/room` with a valid room and deliberately invalid token cookie | 403 in RoomService | 500, gateway `errors` array | Application/gateway error handling defect; source handler attempts JSON parsing of an empty forbidden body; precise deployed implementation unconfirmed |
| CONTRACT-01 | GET `/api/report` with valid token | Published Entry start/end `format: date-time` | `yyyy-MM-dd` strings | Published contract defect; source Entry uses `@JsonFormat(pattern="yyyy-MM-dd")`, agreeing with runtime rather than OpenAPI |

These tests remain enabled and failing. I did not accept 500 as a valid absence/authorization response or remove the date-time constraint. Failure evidence is attached to the Allure report. Only a confirmed specification correction or application fix should resolve them. The shared server was not modified.

## Framework and expectation defects fixed during development

- Null generated IDs were serialized on create. The server rejected room creates with 400 and message creates with 500. NON_NULL serialization now omits absent IDs; isolated workflows pass.
- The initial implementation assumed gateway success envelopes for ID mutation routes. Live execution and Java service code show room update/delete, booking delete and message read/delete return 202; booking update returns 200 with its service response. Assertions now check exact verified statuses and persisted values.
- Message summaries use `id`, while full message objects use `messageid`. Separate typed models now represent both.
- The initial message-read test assumed gateway authentication protection. A disposable-message check established that individual reads are public on this deployment, consistent with the Java MessageController. The test now verifies that actual policy using owned data.
- Failure evidence initially attached after the Allure lifecycle ended. It now attaches in the TestNG invocation hook, and attachment files were verified.
- Schema assertions can include raw response bodies in exception messages. The contract assertion wraps them with a safe failure message, retaining the failure and sanitized evidence without echoing personal fields. This does not alter schema constraints.

## External limitations and excluded behavior

- Inspected upstream gateway source and deployed ID routes differ. Source discovery is not treated as proof of a deployed response format.
- Gateway logout source returns success without revoking the backend token. No expiry/revocation guarantee is tested or claimed.
- Public message collection and individual reads expose contact-related data by design of the Java service. Tests use synthetic contacts and redact personal and free-text fields in HTTP evidence.
- Public reset timing is configurable and was not measured as a fixed ten-minute guarantee. There were no confirmed resets, rate limits, TLS failures or security challenges during completed executions.
- Parallel execution is configurable, but a full parallel run is withheld while the sequential regression has the three external failures above. Sequential integration workflows are stable in the verified run.
- GitHub-hosted runner accessibility must be assessed from the actual Actions run. If a future runner receives a security challenge, use an authorized self-hosted runner on an accessible network; do not evade the control.
- Interrupted JVMs cannot run finally cleanup. The repository does not contain a dangerous global cleanup utility. Owned identity correlation and environment resets limit residue, but do not make hard-kill cleanup guaranteed.
