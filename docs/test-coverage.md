# Executable coverage

The regression contains **61 test invocations**, including DataProvider rows. Workflows contain multiple assertions and are counted once. Scope groups overlap.

| Class / scope | Invocations | Behavior |
|---|---:|---|
| AuthTests | 7 | Valid login, token validation/reuse, three invalid credential shapes, missing/invalid token, missing logout token |
| RoomTests | 15 | Collection/detail consistency, isolated CRUD, seven invalid input shapes, accepted price boundaries, missing/invalid IDs, missing/invalid session |
| BookingTests | 16 | Authenticated room filter, missing filter/session, full cross-service workflow, conflict rejection, nine invalid inputs, reversed dates, nonexistent booking |
| MessageTests | 13 | Public collection, isolated create/read/mark-read/delete with repeated mark-read and persisted state, ten invalid input rows, public individual read policy |
| BrandingReportTests | 4 | Branding contact/location, unauthorized branding update and report read, authenticated report structure/dates |
| ContractTests | 6 | Five published read contracts and health status |

Smoke contains 6 invocations. Contract contains 6 (including health). Integration contains 4 workflow invocations. The entire regression contains all 61. Negative and boundary groups are additional tags used by the same tests.

Dynamic correlation uses unique roomName → roomid, unique message subject → summary id → messageid, and create-booking response → bookingid. No runtime booking ID or token is hardcoded. Invalid booking inputs are sent against disposable owned rooms. Shared room IDs are retrieved for read-only filtering. Sentinel Integer.MAX_VALUE is used only to query an absent record, never to delete one.

The main integration workflow verifies: create room → create booking → reread booking → list by room → correlate booking-generated message and its synthetic email/check-in → verify dates in room report → verify room unavailable → update booking → verify surname/deposit → cleanup and absence checks. Separate conflict testing proves only one booking persists for an overlapping interval. Message idempotency checks repeat mark-read and verify its state.

Deliberate omissions are documented in the test strategy: shared branding writes, unsupported arbitrary message update, assumed token expiry/revocation, aggressive parallelism, and performance testing. Additional malformed-date combinations and nonexistent-room booking association are future exploration work; they are not claimed as covered.
