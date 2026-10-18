package framework.clients;

import framework.models.Models.*;
import framework.specifications.Specs;
import framework.utils.Json;
import io.restassured.response.Response;

public final class AuthClient {
  public Response login(Object credentials) {
    return Specs.request(null).body(Json.write(credentials)).post("/auth/login");
  }

  public Response validate(Object token) {
    return Specs.request(null).body(Json.write(token)).post("/auth/validate");
  }

  public Response logout(Object token) {
    return Specs.request(null).body(Json.write(token)).post("/auth/logout");
  }
}
