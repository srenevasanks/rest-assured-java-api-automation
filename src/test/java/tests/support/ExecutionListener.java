package tests.support;

import framework.config.Config;
import framework.specifications.Evidence;
import framework.utils.Json;
import io.qameta.allure.Allure;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.*;

public final class ExecutionListener
    implements ITestListener, IExecutionListener, IInvokedMethodListener {
  private final List<Map<String, Object>> rows = new CopyOnWriteArrayList<>();

  public void afterInvocation(IInvokedMethod method, ITestResult result) {
    if (method.isTestMethod() && !result.isSuccess())
      Allure.addAttachment("Sanitized HTTP evidence", "text/plain", Evidence.snapshot());
  }

  public void onTestStart(ITestResult r) {
    Evidence.clear();
  }

  private void record(ITestResult r, String state) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("name", r.getMethod().getQualifiedName());
    row.put("groups", r.getMethod().getGroups());
    row.put("parameters", Arrays.stream(r.getParameters()).map(String::valueOf).toList());
    row.put("status", state);
    row.put("durationMs", r.getEndMillis() - r.getStartMillis());
    if (r.getThrowable() != null) row.put("failure", r.getThrowable().getClass().getName());
    rows.add(row);
    if (!state.equals("passed")) {
      org.slf4j.LoggerFactory.getLogger(ExecutionListener.class)
          .error("{} {}\n{}", state, r.getMethod().getMethodName(), Evidence.snapshot());
    }
    Evidence.clear();
  }

  public void onTestSuccess(ITestResult r) {
    record(r, "passed");
  }

  public void onTestFailure(ITestResult r) {
    record(r, "failed");
  }

  public void onTestSkipped(ITestResult r) {
    record(r, "skipped");
  }

  public void onExecutionFinish() {
    try {
      Path target = Path.of("target");
      Files.createDirectories(target.resolve("allure-results"));
      Map<String, Object> summary = new LinkedHashMap<>();
      summary.put("baseUri", Config.get("base.uri"));
      summary.put("generatedAt", java.time.Instant.now().toString());
      summary.put("total", rows.size());
      for (String s : List.of("passed", "failed", "skipped"))
        summary.put(s, rows.stream().filter(r -> r.get("status").equals(s)).count());
      Map<String, Object> scopes = new LinkedHashMap<>();
      for (String scope : List.of("smoke", "regression", "contract", "integration")) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String state : List.of("passed", "failed", "skipped"))
          counts.put(
              state,
              rows.stream()
                  .filter(
                      r ->
                          Arrays.asList((String[]) r.get("groups")).contains(scope)
                              && r.get("status").equals(state))
                  .count());
        scopes.put(scope, counts);
      }
      summary.put("scopes", scopes);
      summary.put("tests", rows);
      Files.writeString(
          target.resolve("execution-summary.json"),
          Json.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(summary));
      Files.writeString(
          target.resolve("allure-results/environment.properties"),
          "Base.URI="
              + Config.get("base.uri")
              + "\nJava="
              + System.getProperty("java.version")
              + "\nWorkers="
              + System.getProperty("workers", "1")
              + "\n");
      Files.writeString(
          target.resolve("allure-results/categories.json"),
          "[{\"name\":\"Assertion or contract failure (triage"
              + " required)\",\"matchedStatuses\":[\"failed\"]},{\"name\":\"Infrastructure or"
              + " framework exception (triage required)\",\"matchedStatuses\":[\"broken\"]}]");
    } catch (Exception e) {
      throw new IllegalStateException("Cannot write execution evidence", e);
    }
  }
}
