package tests.booking;

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

public class BookingTests {
  @Test(groups = {"smoke", "regression"})
  public void filteredBookings() {
    int id =
        Specs.model(status(new RoomClient().list(null), 200), Rooms.class).rooms().get(0).roomid();
    Bookings data =
        Specs.model(status(new BookingClient().list(id, Session.login()), 200), Bookings.class);
    assertNotNull(data.bookings());
    for (Booking b : data.bookings()) assertEquals(b.roomid(), id);
  }

  @Test(groups = {"regression", "negative"})
  public void listRequiresRoomFilter() {
    assertEquals(
        status(new BookingClient().list(Session.login()), 400).jsonPath().getString("error"),
        "Room ID is required");
  }

  @Test(groups = {"regression", "authorization"})
  public void listRequiresAuthentication() {
    assertEquals(
        status(new BookingClient().list(1, null), 401).jsonPath().getString("error"),
        "Authentication required");
  }

  @Test(groups = {"regression", "integration"})
  public void bookingRoomMessageReportWorkflow() {
    try (var owned = new OwnedData(Session.login())) {
      Room room = owned.room();
      Booking booking = owned.booking(room.roomid());
      Booking read =
          Specs.model(
              status(owned.bookings.get(booking.bookingid(), owned.token), 200), Booking.class);
      assertEquals(read.roomid(), room.roomid());
      assertEquals(read.firstname(), booking.firstname());
      assertEquals(read.bookingdates(), booking.bookingdates());
      assertTrue(read.depositpaid());
      var list =
          Specs.model(status(owned.bookings.list(room.roomid(), owned.token), 200), Bookings.class)
              .bookings();
      assertTrue(list.stream().anyMatch(b -> b.bookingid().equals(booking.bookingid())));
      var messages =
          owned.allMessages().stream()
              .filter(m -> (booking.firstname() + " " + booking.lastname()).equals(m.name()))
              .toList();
      assertEquals(messages.size(), 1, "Booking generated a uniquely correlated message");
      Message msg =
          Specs.model(
              status(owned.messages.get(messages.get(0).id(), owned.token), 200), Message.class);
      assertEquals(msg.email(), booking.email());
      assertTrue(msg.description().contains(booking.bookingdates().checkin().toString()));
      var report =
          Specs.model(
              status(new ReportClient().room(room.roomid(), owned.token), 200), Report.class);
      assertTrue(
          report.report().stream()
              .anyMatch(
                  e ->
                      e.start().equals(booking.bookingdates().checkin())
                          && e.end().equals(booking.bookingdates().checkout())),
          "Room report contains the owned booking dates");
      assertFalse(
          Specs.model(
                  status(
                      owned.rooms.available(
                          booking.bookingdates().checkin().toString(),
                          booking.bookingdates().checkout().toString()),
                      200),
                  Rooms.class)
              .rooms()
              .stream()
              .anyMatch(r -> r.roomid().equals(room.roomid())),
          "Booked room unavailable");
      Booking update =
          new Booking(
              booking.bookingid(),
              room.roomid(),
              booking.firstname(),
              "Updated",
              false,
              booking.bookingdates(),
              null,
              null);
      status(owned.bookings.update(booking.bookingid(), update, owned.token), 200);
      Booking changed =
          Specs.model(
              status(owned.bookings.get(booking.bookingid(), owned.token), 200), Booking.class);
      assertEquals(changed.lastname(), "Updated");
      assertFalse(changed.depositpaid());
    }
  }

  @Test(groups = {"regression", "integration", "negative"})
  public void overlappingBookingRejected() {
    try (var owned = new OwnedData(Session.login())) {
      Room r = owned.room();
      Booking b = owned.booking(r.roomid());
      status(
          owned.bookings.create(
              new Booking(
                  null, r.roomid(), "Another", "Customer", true, b.bookingdates(), null, null),
              null),
          409);
      assertEquals(
          Specs.model(status(owned.bookings.list(r.roomid(), owned.token), 200), Bookings.class)
              .bookings()
              .size(),
          1);
    }
  }

  @DataProvider
  public Object[][] invalidBookings() {
    return new Object[][] {
      {"firstname", null},
      {"firstname", "ab"},
      {"firstname", "a".repeat(19)},
      {"lastname", null},
      {"lastname", "ab"},
      {"lastname", "a".repeat(31)},
      {"roomid", 0},
      {"email", "invalid"},
      {"phone", "123"}
    };
  }

  @Test(
      dataProvider = "invalidBookings",
      groups = {"regression", "negative", "boundary"})
  public void invalidBookingContract(String field, Object value) {
    try (var owned = new OwnedData(Session.login())) {
      Room room = owned.room();
      Booking data = DataFactory.booking(room.roomid());
      owned.trackBooking(data.firstname());
      var node = (com.fasterxml.jackson.databind.node.ObjectNode) Json.MAPPER.valueToTree(data);
      if (value == null) node.remove(field);
      else node.set(field, Json.MAPPER.valueToTree(value));
      status(owned.bookings.create(node, null), 400);
    }
  }

  @Test(groups = {"regression", "negative"})
  public void reversedDatesRejected() {
    try (var owned = new OwnedData(Session.login())) {
      Room room = owned.room();
      Booking b = DataFactory.booking(room.roomid());
      status(
          owned.bookings.create(
              new Booking(
                  null,
                  room.roomid(),
                  b.firstname(),
                  b.lastname(),
                  true,
                  new Dates(b.bookingdates().checkout(), b.bookingdates().checkin()),
                  null,
                  null),
              null),
          409);
    }
  }

  @Test(groups = {"regression", "negative"})
  public void nonexistentBooking() {
    status(new BookingClient().get(Integer.MAX_VALUE, Session.login()), 404);
  }
}
