package tests.message;

import static org.testng.Assert.*;
import static tests.support.Checks.*;

import framework.authentication.Session;
import framework.clients.*;
import framework.data.DataFactory;
import framework.models.Models.*;
import framework.specifications.Specs;
import framework.utils.Json;
import org.testng.annotations.*;
import tests.support.OwnedData;

public class MessageTests {
  @Test(groups = {"smoke", "regression"})
  public void publicMessageList() {
    assertNotNull(
        Specs.model(status(new MessageClient().list(null), 200), Messages.class).messages());
  }

  @Test(groups = {"regression", "integration"})
  public void createReadMarkReadDelete() {
    try (var owned = new OwnedData(Session.login())) {
      Message m = owned.message();
      assertTrue(m.email().endsWith("@example.test"));
      assertEquals(m.phone(), "01234567890");
      assertEquals(m.description(), "Synthetic message for isolated API testing.");
      status(owned.messages.markRead(m.messageid(), owned.token), 202);
      status(owned.messages.markRead(m.messageid(), owned.token), 202);
      assertEquals(
          Specs.model(status(owned.messages.get(m.messageid(), owned.token), 200), Message.class),
          m);
      assertTrue(
          owned.allMessages().stream().anyMatch(s -> s.id() == m.messageid() && s.read()),
          "Read state persisted");
    }
  }

  @DataProvider
  public Object[][] invalidMessages() {
    return new Object[][] {
      {"name", null},
      {"name", " "},
      {"email", null},
      {"email", "invalid"},
      {"phone", "1".repeat(10)},
      {"phone", "1".repeat(22)},
      {"subject", "a".repeat(4)},
      {"subject", "a".repeat(101)},
      {"description", "a".repeat(19)},
      {"description", "a".repeat(2001)}
    };
  }

  @Test(
      dataProvider = "invalidMessages",
      groups = {"regression", "negative", "boundary"})
  public void invalidMessageContract(String field, Object value) {
    var node =
        (com.fasterxml.jackson.databind.node.ObjectNode)
            Json.MAPPER.valueToTree(DataFactory.message());
    if (value == null) node.remove(field);
    else node.set(field, Json.MAPPER.valueToTree(value));
    var r = status(new MessageClient().create(node, null), 400);
    assertFalse(r.asString().isBlank());
  }

  @Test(groups = {"regression", "authorization"})
  public void individualMessageIsPublic() {
    try (var owned = new OwnedData(Session.login())) {
      Message m = owned.message();
      assertEquals(
          Specs.model(status(owned.messages.get(m.messageid(), null), 200), Message.class), m);
    }
  }
}
