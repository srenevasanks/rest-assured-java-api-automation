package framework.clients;

import framework.specifications.Specs;
import framework.utils.Json;
import io.restassured.response.Response;

public final class BrandingClient {
  public Response get() {
    return Specs.request(null).get("/branding");
  }

  public Response update(Object body, String token) {
    return Specs.request(token).body(Json.write(body)).put("/branding");
  }
}
