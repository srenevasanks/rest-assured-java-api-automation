package framework.specifications;

import com.fasterxml.jackson.databind.JsonNode;
import framework.utils.Json;
import io.restassured.filter.*;
import io.restassured.response.Response;
import io.restassured.specification.*;
import java.util.*;

public final class Evidence implements Filter {
  private static final ThreadLocal<List<String>> EVENTS = ThreadLocal.withInitial(ArrayList::new);

  public static void clear() {
    EVENTS.remove();
  }

  public static String snapshot() {
    return String.join("\n\n", EVENTS.get());
  }

  private static void scrub(JsonNode node) {
    if (node.isObject()) {
      var it = node.fields();
      while (it.hasNext()) {
        var field = it.next();
        if (field
            .getKey()
            .matches(
                "(?i)username|.*password.*|.*token.*|authorization|cookie|set-cookie|email|phone|firstname|lastname|name|description|subject|title"))
          ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(field.getKey(), "[REDACTED]");
        else scrub(field.getValue());
      }
    } else if (node.isArray()) node.forEach(Evidence::scrub);
  }

  public static String sanitize(String body) {
    if (body == null || body.isBlank()) return "<empty>";
    try {
      var node = Json.MAPPER.readTree(body);
      scrub(node);
      return Json.MAPPER.writeValueAsString(node);
    } catch (Exception e) {
      return "<non-JSON body omitted>";
    }
  }

  public Response filter(
      FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    String request =
        req.getMethod()
            + " "
            + req.getURI()
            + "\n"
            + "Accept: application/json; Content-Type: application/json\n"
            + "Credentials/cookies/headers omitted\n"
            + sanitize(req.getBody() == null ? null : req.getBody().toString());
    try {
      Response response = ctx.next(req, res);
      EVENTS
          .get()
          .add(request + "\nHTTP " + response.statusCode() + "\n" + sanitize(response.asString()));
      return response;
    } catch (RuntimeException e) {
      EVENTS.get().add(request + "\nTransport failure: " + e.getClass().getSimpleName());
      throw e;
    }
  }
}
