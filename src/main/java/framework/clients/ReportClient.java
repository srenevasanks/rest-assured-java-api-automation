package framework.clients;

import framework.specifications.Specs;
import io.restassured.response.Response;

public final class ReportClient {
  public Response get(String token) {
    return Specs.request(token).get("/report");
  }

  public Response room(int id, String token) {
    return Specs.request(token).get("/report/room/" + id);
  }
}
