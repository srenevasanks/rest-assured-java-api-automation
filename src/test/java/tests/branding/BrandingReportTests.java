package tests.branding;

import static org.testng.Assert.*;
import static tests.support.Checks.*;

import framework.authentication.Session;
import framework.clients.*;
import framework.models.Models.*;
import framework.specifications.Specs;
import org.testng.annotations.*;

public class BrandingReportTests {
  @Test(groups = {"smoke", "regression"})
  public void brandingHasContactAndLocation() {
    Branding b = Specs.model(status(new BrandingClient().get(), 200), Branding.class);
    assertFalse(b.name().isBlank());
    assertNotNull(b.contact());
    assertTrue(b.contact().email().contains("@"));
    assertNotNull(b.address());
    assertTrue(Math.abs(b.map().latitude()) <= 90);
    assertTrue(Math.abs(b.map().longitude()) <= 180);
  }

  @Test(groups = {"regression", "authorization"})
  public void brandingUpdateRequiresSession() {
    assertEquals(
        status(new BrandingClient().update("{}", null), 401).jsonPath().getString("error"),
        "Authentication required");
  }

  @Test(groups = {"regression", "authorization"})
  public void reportsRequireSession() {
    assertEquals(
        status(new ReportClient().get(null), 401).jsonPath().getString("error"),
        "Authentication required");
  }

  @Test(groups = {"regression"})
  public void authenticatedReport() {
    Report r = Specs.model(status(new ReportClient().get(Session.login()), 200), Report.class);
    assertNotNull(r.report());
    for (Entry e : r.report()) {
      assertNotNull(e.start());
      assertNotNull(e.end());
      assertFalse(e.end().isBefore(e.start()));
      assertNotNull(e.title());
    }
  }
}
