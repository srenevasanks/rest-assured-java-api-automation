package framework.data;

import framework.models.Models.*;
import java.time.LocalDate;
import java.util.*;

public final class DataFactory {
  public static String unique() {
    return "SDET" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
  }

  public static Room room() {
    return new Room(
        null,
        unique(),
        "Single",
        true,
        "/images/room1.jpg",
        "Disposable automated scenario",
        List.of("WiFi", "TV"),
        123);
  }

  public static Message message() {
    String id = unique();
    return new Message(
        null,
        id,
        id + "@example.test",
        "01234567890",
        "Test " + id,
        "Synthetic message for isolated API testing.");
  }

  public static Booking booking(int room) {
    String id = unique();
    LocalDate in = LocalDate.now().plusDays(35);
    return new Booking(
        null,
        room,
        id,
        "Automation",
        true,
        new Dates(in, in.plusDays(2)),
        id + "@example.test",
        "01234567890");
  }

  private DataFactory() {}
}
