package framework.clients;

import framework.specifications.Specs;
import framework.utils.Json;
import io.restassured.response.Response;

public final class MessageClient {
  public Response list(String token) {
    return Specs.request(token).get("/message");
  }

  public Response get(int id, String token) {
    return Specs.request(token).get("/message/" + id);
  }

  public Response create(Object body, String token) {
    return Specs.request(token).body(Json.write(body)).post("/message");
  }

  public Response delete(int id, String token) {
    return Specs.request(token).delete("/message/" + id);
  }

  public Response count() {
    return Specs.request(null).get("/message/count");
  }

  public Response markRead(int id, String token) {
    return Specs.request(token).put("/message/" + id + "/read");
  }
}
