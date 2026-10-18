package tests.support;

import static org.testng.Assert.*;

import io.restassured.response.Response;

public final class Checks {
  public static Response status(Response r, int expected) {
    assertEquals(r.statusCode(), expected, "HTTP status (sanitized evidence attached on failure)");
    return r;
  }

  public static void success(Response r) {
    status(r, 200);
    assertEquals(r.jsonPath().getBoolean("success"), Boolean.TRUE);
  }

  private Checks() {}
}
