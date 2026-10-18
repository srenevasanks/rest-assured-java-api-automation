# Schema provenance

The six unmodified OpenAPI 3.1 documents in `specifications/` were retrieved from the public `/api/{service}/v3/api-docs` endpoints on 18 October 2026. They describe Java services, while the public Next.js gateway transforms selected responses.

The executable schemas under `src/test/resources/schemas` are derived from those component definitions, with local references rewritten from `#/components/schemas/` to `#/definitions/` and a draft-04 envelope for Rest Assured 5.5.6's validator. These models use the shared draft-04 subset. The original documents remain available for auditing. No schema was inferred solely from a sample response.

Booking read schemas remove `email` and `phone` from `required`. The upstream `Booking(ResultSet)` constructor does not populate these request-only contact fields, and `@JsonInclude(NON_NULL)` omits them. Both fields remain typed and constrained when supplied. The public read response confirms their omission. This is a request/read model distinction, not an assertion relaxed after a failing test.

Collection envelope fields and persisted model fields are additionally required based on the source serializers and constructors. Unknown properties remain permitted for additive evolution. Nullable and optional input fields use boxed IDs and NON_NULL serialization in Java. Read-only room/report/message/branding collection contracts use the published types; gateway `success` envelopes are asserted separately. Generated OpenAPI success-code annotations are incomplete, so status assertions also use controller/service code and verified live behavior.

Auth, gateway errors and actuator health use explicit assertions derived from their handlers. Error responses are not forced into one invented schema. Message summaries use `id` and `read`; full messages use `messageid`. Booking dates use `bookingdates`; summary records use `bookingDates` in the published service contract.
