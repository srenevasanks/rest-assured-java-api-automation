package framework.clients;

import framework.specifications.Specs;
import framework.utils.Json;
import io.restassured.response.Response;

public final class BookingClient {
  public Response list(String token) {
    return Specs.request(token).get("/booking");
  }

  public Response get(int id, String token) {
    return Specs.request(token).get("/booking/" + id);
  }

  public Response create(Object body, String token) {
    return Specs.request(token).body(Json.write(body)).post("/booking");
  }

  public Response update(int id, Object body, String token) {
    return Specs.request(token).body(Json.write(body)).put("/booking/" + id);
  }

  public Response delete(int id, String token) {
    return Specs.request(token).delete("/booking/" + id);
  }

  public Response list(int room, String token) {
    return Specs.request(token).queryParam("roomid", room).get("/booking");
  }

  public Response summary(int room, String token) {
    return Specs.request(token).queryParam("roomid", room).get("/booking/summary");
  }
}
