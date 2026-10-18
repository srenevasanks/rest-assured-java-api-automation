package tests.auth;

import static org.testng.Assert.*;
import static tests.support.Checks.*;

import framework.authentication.Session;
import framework.clients.AuthClient;
import framework.config.Config;
import framework.models.Models.*;
import org.testng.annotations.*;

public class AuthTests {
  @Test(groups = {"smoke", "regression"})
  public void validSessionCanBeReused() {
    String token = Session.login();
    AuthClient c = new AuthClient();
    for (int i = 0; i < 2; i++) {
      var r = status(c.validate(new Token(token)), 200);
      assertEquals(r.jsonPath().getBoolean("valid"), Boolean.TRUE);
    }
  }

  @DataProvider
  public Object[][] invalidLogins() {
    return new Object[][] {{"wrong-password"}, {"missing-password"}, {"missing-username"}};
  }

  @Test(
      dataProvider = "invalidLogins",
      groups = {"regression", "negative", "authorization"})
  public void invalidCredentials(String scenario) {
    String body =
        switch (scenario) {
          case "wrong-password" ->
              "{\"username\":\""
                  + Config.credential("API_USERNAME")
                  + "\",\"password\":\"deliberately-invalid\"}";
          case "missing-password" -> "{\"username\":\"" + Config.credential("API_USERNAME") + "\"}";
          default -> "{}";
        };
    var r = status(new AuthClient().login(body), 401);
    assertEquals(r.jsonPath().getString("error"), "Invalid credentials");
  }

  @Test(groups = {"regression", "negative"})
  public void missingToken() {
    assertEquals(
        status(new AuthClient().validate("{}"), 401).jsonPath().getString("error"),
        "No token provided");
  }

  @Test(groups = {"regression", "negative", "authorization"})
  public void invalidToken() {
    assertEquals(
        status(new AuthClient().validate(new Token("deliberately-invalid")), 403)
            .jsonPath()
            .getString("error"),
        "Invalid token");
  }

  @Test(groups = {"regression", "negative"})
  public void logoutRequiresToken() {
    assertEquals(
        status(new AuthClient().logout("{}"), 400).jsonPath().getString("message"),
        "Token is required");
  }
}
