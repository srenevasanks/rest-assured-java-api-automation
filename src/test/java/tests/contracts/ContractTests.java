package tests.contracts;

import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.testng.Assert.*;
import static tests.support.Checks.*;

import framework.authentication.Session;
import framework.clients.*;
import framework.models.Models.*;
import framework.specifications.Specs;
import org.testng.annotations.*;

public class ContractTests {
  @DataProvider
  public Object[][] contracts() {
    return new Object[][] {
      {"room", "Rooms"},
      {"message", "Messages"},
      {"branding", "Branding"},
      {"report", "Report"},
      {"booking", "Bookings"}
    };
  }

  @Test(
      dataProvider = "contracts",
      groups = {"regression", "contract"})
  public void publishedReadContracts(String service, String schema) {
    var response =
        switch (service) {
          case "room" -> new RoomClient().list(null);
          case "message" -> new MessageClient().list(null);
          case "branding" -> new BrandingClient().get();
          case "report" -> new ReportClient().get(Session.login());
          default -> {
            int room =
                Specs.model(status(new RoomClient().list(null), 200), Rooms.class)
                    .rooms()
                    .get(0)
                    .roomid();
            yield new BookingClient().list(room, Session.login());
          }
        };
    try {
      status(response, 200)
          .then()
          .spec(Specs.json())
          .body(matchesJsonSchemaInClasspath("schemas/" + service + "-" + schema + ".json"));
    } catch (AssertionError e) {
      throw new AssertionError(
          "Published schema mismatch for "
              + service
              + "/"
              + schema
              + "; compare sanitized HTTP evidence with the versioned schema");
    }
  }

  @Test(groups = {"regression", "contract", "smoke"})
  public void healthContract() {
    var response = status(new RoomClient().health(), 200);
    assertEquals(response.jsonPath().getString("status"), "UP");
  }
}
