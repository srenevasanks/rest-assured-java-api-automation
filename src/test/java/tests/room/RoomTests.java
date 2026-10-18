package tests.room;

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

public class RoomTests {
  @Test(groups = {"smoke", "regression"})
  public void roomsAreReadable() {
    Rooms rooms = Specs.model(status(new RoomClient().list(null), 200), Rooms.class);
    assertNotNull(rooms.rooms());
    assertFalse(rooms.rooms().isEmpty());
    Room r = rooms.rooms().get(0);
    Room detail = Specs.model(status(new RoomClient().get(r.roomid(), null), 200), Room.class);
    assertEquals(detail, r);
  }

  @Test(groups = {"regression", "integration"})
  public void isolatedRoomCrud() {
    try (var owned = new OwnedData(Session.login())) {
      Room created = owned.room();
      Room read = Specs.model(status(owned.rooms.get(created.roomid(), null), 200), Room.class);
      assertEquals(read, created);
      Room updated =
          new Room(
              created.roomid(),
              created.roomName(),
              "Suite",
              false,
              created.image(),
              "Updated disposable room",
              java.util.List.of("WiFi"),
              250);
      status(owned.rooms.update(created.roomid(), updated, owned.token), 202);
      assertEquals(
          Specs.model(status(owned.rooms.get(created.roomid(), null), 200), Room.class), updated);
    }
  }

  @DataProvider
  public Object[][] invalidRooms() {
    return new Object[][] {
      {"roomName", null},
      {"roomName", ""},
      {"type", null},
      {"type", "Penthouse"},
      {"roomPrice", 0},
      {"roomPrice", 1000},
      {"roomPrice", "not-a-number"}
    };
  }

  @Test(
      dataProvider = "invalidRooms",
      groups = {"regression", "negative", "boundary"})
  public void invalidRoomContract(String field, Object value) {
    var node = Json.MAPPER.valueToTree(DataFactory.room());
    if (value == null) ((com.fasterxml.jackson.databind.node.ObjectNode) node).remove(field);
    else
      ((com.fasterxml.jackson.databind.node.ObjectNode) node)
          .set(field, Json.MAPPER.valueToTree(value));
    var r = status(new RoomClient().create(node, Session.login()), 400);
    assertFalse(r.asString().isBlank(), "Validation response supplied");
  }

  @DataProvider
  public Object[][] priceBoundaries() {
    return new Object[][] {{1}, {999}};
  }

  @Test(
      dataProvider = "priceBoundaries",
      groups = {"regression", "boundary"})
  public void acceptedPriceBoundaries(int price) {
    try (var owned = new OwnedData(Session.login())) {
      Room r = owned.room();
      Room updated =
          new Room(
              r.roomid(),
              r.roomName(),
              r.type(),
              r.accessible(),
              r.image(),
              r.description(),
              r.features(),
              price);
      status(owned.rooms.update(r.roomid(), updated, owned.token), 202);
      assertEquals(
          Specs.model(status(owned.rooms.get(r.roomid(), null), 200), Room.class).roomPrice(),
          price);
    }
  }

  @Test(groups = {"regression", "negative"})
  public void nonexistentRoomReturnsNotFound() {
    status(new RoomClient().get(Integer.MAX_VALUE, null), 404);
  }

  @Test(groups = {"regression", "negative"})
  public void invalidRoomIdentifier() {
    status(new RoomClient().get("not-a-number", null), 404);
  }

  @Test(groups = {"regression", "authorization"})
  public void createRequiresSession() {
    var r = status(new RoomClient().create(DataFactory.room(), null), 401);
    assertTrue(r.asString().contains("Authentication required"));
  }

  @Test(groups = {"regression", "authorization"})
  public void invalidSessionCannotCreateRoom() {
    status(new RoomClient().create(DataFactory.room(), "deliberately-invalid"), 403);
  }
}
