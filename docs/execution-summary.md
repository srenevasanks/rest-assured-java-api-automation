# Actual execution evidence

Executed on **18 October 2026**, Windows 11, Java 17.0.12, Maven 3.9.11, sequential workers=1. Public target: `https://automationintesting.online/api`. The local execution sandbox required authorized network access and a workspace Maven cache; the application itself remained public and remote.

## Final local results

| Execution | Total | Passed | Failed | Skipped |
|---|---:|---:|---:|---:|
| Dedicated smoke | 6 | 6 | 0 | 0 |
| Dedicated integration | 4 | 4 | 0 | 0 |
| Dedicated contract | 6 | 5 | 1 | 0 |
| Final regression XML | 61 | 58 | 3 | 0 |
| Smoke scope within final regression | 6 | 6 | 0 | 0 |
| Integration scope within final regression | 4 | 4 | 0 | 0 |

Final regression completed at **13:12 IST**. Maven's total duration was 1 minute 54 seconds; Allure reports approximately 108 seconds across test invocations. Scope counts overlap and are not additive. Results are not a claim about future public-environment availability.

Machine-readable evidence: `reports/regression/execution-summary.json`, plus corresponding smoke/contract/integration directories. The initial full default command produced 57 passes and 4 failures; one incorrect message authorization expectation was subsequently corrected through a disposable-message probe. Final regression produced 58 passes and 3 external failures. Initial integration summaries retain the development failures rather than hiding that debugging occurred.

## Remaining failures

1. `RoomTests.nonexistentRoomReturnsNotFound`: expected 404 from service contract; observed 500.
2. `RoomTests.invalidSessionCannotCreateRoom`: expected 403 from service authorization contract; observed gateway 500.
3. `ContractTests.publishedReadContracts(report, Report)`: published date-time fields contain date-only values.

All three remain enabled. [Known issues](known-issues.md) records evidence, classifications and the limits of source-based root-cause inference. No confirmed rate limit, TLS failure, Cloudflare challenge or public reset occurred. The final regression reported no cleanup failure and no skipped tests. A full parallel regression was not run while these sequential external failures remain.

## Commands verified

```shell
mvn -DskipTests test-compile
mvn clean test
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
mvn clean test -DsuiteXmlFile=src/test/resources/suites/regression.xml
mvn clean test -DsuiteXmlFile=src/test/resources/suites/contract.xml
mvn clean test -DsuiteXmlFile=src/test/resources/suites/integration.xml
mvn allure:report
mvn allure:serve
```

Java 17 must be on PATH as well as JAVA_HOME for the Allure launcher. The documented test-account environment variables must be set. Regression and contract commands correctly return nonzero for unresolved failures. Each clean removes prior target results; saved scope snapshots in `reports/` are separate.

`mvn allure:report` completed successfully. `mvn allure:serve` started its HTTP server; GET `/widgets/summary.json` returned 200 with **61 total, 58 passed, 3 failed, 0 skipped**. The verification server was then stopped. Restart it with the same command to explore the report; use Ctrl+C to stop it.

Delivered HTML: `reports/allure-html/index.html`. Generated Maven HTML: `target/site/allure-maven-plugin/index.html`. The report contains 61 test-case JSON files and one HTTP evidence attachment for each of the three failures. Environment and category widgets were inspected. A credential-pattern scan across saved reports found no unredacted password/token JSON values.

## CI validation

The workflow passed actionlint 1.7.12 with exit code 0. It compiles separately, executes smoke sequentially, preserves test failure exits, and always uploads available results/report artifacts. Actual hosted-run status is recorded after publication below.

## Delivery

The framework is a separate Git repository named `rest-assured-java-api-automation`. Existing Selenium and Playwright repositories were not modified. No local application was installed or hosted. Published OpenAPI documents and discovered differences are recorded in Phase 0 and schema provenance.
