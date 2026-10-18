package framework.clients;

import framework.specifications.Specs;
import framework.utils.Json;
import io.restassured.response.Response;

public final class RoomClient {
  public Response list(String token) {
    return Specs.request(token).get("/room");
  }

  public Response get(int id, String token) {
    return Specs.request(token).get("/room/" + id);
  }

  public Response get(String id, String token) {
    return Specs.request(token).pathParam("id", id).get("/room/{id}");
  }

  public Response health() {
    return Specs.request(null).get("/room/actuator/health");
  }

  public Response create(Object body, String token) {
    return Specs.request(token).body(Json.write(body)).post("/room");
  }

  public Response update(int id, Object body, String token) {
    return Specs.request(token).body(Json.write(body)).put("/room/" + id);
  }

  public Response delete(int id, String token) {
    return Specs.request(token).delete("/room/" + id);
  }

  public Response available(String in, String out) {
    return Specs.request(null).queryParam("checkin", in).queryParam("checkout", out).get("/room");
  }
}
