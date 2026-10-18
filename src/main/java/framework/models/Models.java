package framework.models;

import com.fasterxml.jackson.annotation.*;
import java.time.LocalDate;
import java.util.List;

public final class Models {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Login(String username, String password) {
    @Override
    public String toString() {
      return "Login[REDACTED]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Token(String token) {
    @Override
    public String toString() {
      return "Token[REDACTED]";
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Room(
      Integer roomid,
      String roomName,
      String type,
      boolean accessible,
      String image,
      String description,
      List<String> features,
      int roomPrice) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Rooms(List<Room> rooms) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Dates(LocalDate checkin, LocalDate checkout) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Booking(
      Integer bookingid,
      int roomid,
      String firstname,
      String lastname,
      boolean depositpaid,
      Dates bookingdates,
      String email,
      String phone) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Bookings(List<Booking> bookings) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Message(
      Integer messageid,
      String name,
      String email,
      String phone,
      String subject,
      String description) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record MessageSummary(int id, String name, String subject, boolean read) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Messages(List<MessageSummary> messages) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Entry(LocalDate start, LocalDate end, String title) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Report(List<Entry> report) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Contact(String name, String phone, String email) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Address(
      String line1, String line2, String postTown, String county, String postCode) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record MapLocation(double latitude, double longitude) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Branding(
      String name,
      String logoUrl,
      String description,
      String directions,
      Contact contact,
      Address address,
      @JsonProperty("map") MapLocation map) {}

  private Models() {}
}
