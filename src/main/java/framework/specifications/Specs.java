package framework.specifications;

import framework.config.Config;
import framework.utils.Json;
import io.restassured.builder.*;
import io.restassured.config.*;
import io.restassured.specification.*;

public final class Specs {
  private static long nextRequest;

  private static synchronized void pace() {
    long now = System.currentTimeMillis();
    long wait = nextRequest - now;
    if (wait > 0)
      try {
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
    nextRequest = System.currentTimeMillis() + Config.number("request.interval.ms");
  }

  public static RequestSpecification request(String token) {
    pace();
    var b =
        new RequestSpecBuilder()
            .setBaseUri(Config.get("base.uri"))
            .setBasePath(Config.get("base.path"))
            .setContentType("application/json")
            .setAccept("application/json")
            .setConfig(
                RestAssuredConfig.config()
                    .redirect(RedirectConfig.redirectConfig().followRedirects(false))
                    .httpClient(
                        HttpClientConfig.httpClientConfig()
                            .setParam(
                                "http.connection.timeout", Config.number("connect.timeout.ms"))
                            .setParam("http.socket.timeout", Config.number("read.timeout.ms"))))
            .addFilter(new Evidence());
    if (token != null) b.addCookie("token", token);
    return io.restassured.RestAssured.given().spec(b.build());
  }

  public static ResponseSpecification json() {
    return new ResponseSpecBuilder().expectContentType("application/json").build();
  }

  public static <T> T model(io.restassured.response.Response response, Class<T> type) {
    try {
      return Json.MAPPER.readValue(response.asString(), type);
    } catch (Exception e) {
      throw new IllegalStateException("Response does not match " + type.getSimpleName(), e);
    }
  }

  private Specs() {}
}
