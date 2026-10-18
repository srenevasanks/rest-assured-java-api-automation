# Phase 0: public API feasibility

Validated on 18 October 2026 from Windows using curl 8.x over verified HTTPS. Decision: **proceed**, with public gateway differences explicitly accounted for. No local application was installed or hosted.

## Evidence

| Probe | Actual result |
|---|---|
| GET `/api/room/actuator/health` | 200, actuator JSON, status UP |
| GET `/api/room` | 200, JSON rooms collection |
| POST `/api/auth/login`, documented test account | 200, JSON token |
| GET `/api/booking` with token | 400, Room ID is required |
| GET `/api/booking?roomid=1` with token | 200, bookings collection |
| POST `/api/room` with unique disposable room | 200, success true; gateway omits created ID |
| GET `/api/room` after create | 200; exact unique name correlated to owned ID |
| DELETE owned room | 202 |
| GET deleted room | 500, service Internal Server Error; defect candidate |
| GET `/api/{service}/v3/api-docs` for all six services | 200, OpenAPI 3.1 JSON |

Initial sandbox requests could not resolve DNS. The same requests succeeded with authorized network access. This is an execution sandbox limitation, not evidence of application downtime. No TLS error, challenge page, 403 security block, 429 or unexpected redirect was observed on these API probes. `/room/` returned 308; implementation uses `/api` routes.

## Contract discovery

Base URI: `https://automationintesting.online`; public base path `/api`. JSON requests use Content-Type and Accept application/json. Authentication: login returns token in JSON at the gateway; protected requests send cookie `token`. Token validation accepts JSON `{token: ...}`. No token lifetime is assumed.

The official Postman page was inspected but its dynamic documentation did not expose request content to the text reader. Published OpenAPI snapshots are retained in `specifications/`. Controllers, validation models, gateway handlers and services were inspected at upstream commit `d36bd3f8647a091d406e53bad463c5e3e5d2ece1`. Source is a discovery aid; deployment may differ.

| Service | Discovered operations | Inputs / dependencies |
|---|---|---|
| Auth | POST login, validate, logout | username/password; token body; gateway logout source is a no-op, so revocation must not be assumed |
| Room | GET/POST collection; GET/PUT/DELETE ID | numeric room ID; optional checkin/checkout; cookie for mutations; price 1–999; five room types |
| Booking | GET/POST collection; GET/PUT/DELETE ID; GET summary, unavailable | roomid query required by gateway list/summary; date query for unavailable; cookie for reads/update/delete; create is public |
| Message | GET/POST collection; GET/DELETE ID; PUT ID/read | create is public; gateway requires cookie for individual read/delete/read-state; no arbitrary content update |
| Branding | GET/PUT collection | public read, cookie for update; global shared state is not modified |
| Report | GET collection; GET room/ID | cookie for aggregate; room report relates to booking dates |

OpenAPI generated success codes are incomplete (often only 200) and do not describe gateway transformations. Expected statuses require controller/gateway evidence and execution. Error formats differ: error string, errors array, validation field errors, and Spring error objects. Published schemas also conflate request-only contact fields with booking read responses; schema adaptations must be recorded.

## Stability and safety

Source documents configurable database resets (for example 10 minutes), not a measured fixed public reset interval. Workflows must own their data, complete within one test, use UUID names and dynamic IDs, and clean up in finally/AutoCloseable scopes. Collection presence is used to verify deletion because missing-room GET currently returns 500. No shared records are deleted. Booking-created messages must be correlated by exact synthetic contact identity and cleaned up. Start sequential; parallel verification is limited to two workers. No automatic retries of mutations or failing tests.

Sources: [official collection](https://www.postman.com/automation-in-testing/restful-booker-collections/documentation/ci13ds3/restful-booker-platform), [application source](https://github.com/mwinteringham/restful-booker-platform/tree/d36bd3f8647a091d406e53bad463c5e3e5d2ece1), [room OpenAPI](https://automationintesting.online/api/room/v3/api-docs).
