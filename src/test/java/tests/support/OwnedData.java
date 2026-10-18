package tests.support;

import static org.testng.Assert.*;
import static tests.support.Checks.*;

import framework.clients.*;
import framework.data.DataFactory;
import framework.models.Models.*;
import framework.specifications.Specs;
import java.util.*;

/** Scenario ownership is rechecked before deletion, including after public resets. */
public final class OwnedData implements AutoCloseable {
  public final String token;
  public final RoomClient rooms = new RoomClient();
  public final BookingClient bookings = new BookingClient();
  public final MessageClient messages = new MessageClient();
  private final Set<String> roomNames = new HashSet<>();
  private final Set<String> messageSubjects = new HashSet<>();
  private final Set<String> bookingNames = new HashSet<>();
  private final Set<Integer> roomIds = new LinkedHashSet<>();

  public OwnedData(String token) {
    this.token = token;
  }

  public void trackRoom(String name) {
    roomNames.add(name);
  }

  public void trackMessage(String subject) {
    messageSubjects.add(subject);
  }

  public void trackBooking(String firstname) {
    bookingNames.add(firstname);
  }

  public Room room() {
    Room data = DataFactory.room();
    trackRoom(data.roomName());
    success(rooms.create(data, token));
    List<Room> found =
        roomList().stream().filter(r -> r.roomName().equals(data.roomName())).toList();
    assertEquals(found.size(), 1, "Unique room correlation");
    Room created = found.get(0);
    roomIds.add(created.roomid());
    return created;
  }

  public Message message() {
    Message data = DataFactory.message();
    trackMessage(data.subject());
    success(messages.create(data, null));
    var found = allMessages().stream().filter(m -> m.subject().equals(data.subject())).toList();
    assertEquals(found.size(), 1, "Unique message correlation");
    return Specs.model(status(messages.get(found.get(0).id(), token), 200), Message.class);
  }

  /** Scenario fixture retains original contact details that booking reads intentionally omit. */
  public Booking booking(int room) {
    Booking data = DataFactory.booking(room);
    trackBooking(data.firstname());
    Booking created = Specs.model(status(bookings.create(data, null), 201), Booking.class);
    assertNotNull(created.bookingid(), "Booking ID returned");
    assertEquals(created.firstname(), data.firstname());
    assertEquals(created.bookingdates(), data.bookingdates());
    return new Booking(
        created.bookingid(),
        room,
        data.firstname(),
        data.lastname(),
        data.depositpaid(),
        data.bookingdates(),
        data.email(),
        data.phone());
  }

  public List<MessageSummary> allMessages() {
    return Specs.model(status(messages.list(null), 200), Messages.class).messages();
  }

  private List<Room> roomList() {
    return Specs.model(status(rooms.list(null), 200), Rooms.class).rooms();
  }

  public void close() {
    List<Throwable> errors = new ArrayList<>();
    attempt(
        errors,
        () -> {
          for (Room r : roomList()) if (roomNames.contains(r.roomName())) roomIds.add(r.roomid());
        });
    for (int roomId : roomIds) {
      attempt(
          errors,
          () -> {
            var ownedRoom =
                roomList().stream()
                    .filter(r -> r.roomid() == roomId && roomNames.contains(r.roomName()))
                    .findFirst();
            if (ownedRoom.isEmpty()) {
              resetNotice(roomId);
              return;
            }
            var list =
                Specs.model(status(bookings.list(roomId, token), 200), Bookings.class).bookings();
            for (Booking b : list)
              if (bookingNames.contains(b.firstname())) {
                status(bookings.delete(b.bookingid(), token), 202);
                assertFalse(
                    Specs.model(status(bookings.list(roomId, token), 200), Bookings.class)
                        .bookings()
                        .stream()
                        .anyMatch(current -> current.bookingid().equals(b.bookingid())),
                    "Booking cleanup persisted");
              }
          });
    }
    attempt(
        errors,
        () -> {
          for (MessageSummary m : allMessages()) {
            boolean owned =
                messageSubjects.contains(m.subject())
                    || bookingNames.stream()
                        .anyMatch(n -> m.name() != null && m.name().startsWith(n + " "));
            if (owned) {
              status(messages.delete(m.id(), token), 202);
              assertFalse(
                  allMessages().stream().anyMatch(current -> current.id() == m.id()),
                  "Message cleanup persisted");
            }
          }
        });
    for (int roomId : roomIds)
      attempt(
          errors,
          () -> {
            if (roomList().stream()
                .anyMatch(r -> r.roomid() == roomId && roomNames.contains(r.roomName()))) {
              status(rooms.delete(roomId, token), 202);
              assertFalse(
                  roomList().stream().anyMatch(r -> r.roomid() == roomId),
                  "Room cleanup persisted");
            } else resetNotice(roomId);
          });
    if (!errors.isEmpty()) {
      AssertionError error =
          new AssertionError(
              "Cleanup failed for scenario-owned records; inspect suppressed failures");
      errors.forEach(error::addSuppressed);
      throw error;
    }
  }

  private static void resetNotice(int id) {
    org.slf4j.LoggerFactory.getLogger(OwnedData.class)
        .warn("Owned room {} disappeared or changed identity; possible environment reset", id);
  }

  private static void attempt(List<Throwable> errors, Runnable action) {
    try {
      action.run();
    } catch (AssertionError | RuntimeException e) {
      errors.add(e);
    }
  }
}
