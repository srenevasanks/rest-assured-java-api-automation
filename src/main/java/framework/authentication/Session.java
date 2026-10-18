package framework.authentication;

import framework.clients.AuthClient;
import framework.config.Config;
import framework.models.Models.*;

public final class Session {
  public static String login() {
    var r =
        new AuthClient()
            .login(new Login(Config.credential("API_USERNAME"), Config.credential("API_PASSWORD")));
    if (r.statusCode() != 200)
      throw new IllegalStateException("Authentication failed: HTTP " + r.statusCode());
    String token = r.jsonPath().getString("token");
    if (token == null || token.isBlank())
      throw new IllegalStateException("Login returned no token");
    return token;
  }

  private Session() {}
}
