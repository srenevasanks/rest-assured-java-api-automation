package framework.config;

import java.util.Properties;

public final class Config {
  private static final Properties P = new Properties();

  static {
    try (var in = Config.class.getResourceAsStream("/config/default.properties")) {
      P.load(in);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static String get(String key) {
    String env = System.getenv(key.toUpperCase().replace('.', '_'));
    return System.getProperty(key, env == null ? P.getProperty(key) : env);
  }

  public static int number(String key) {
    return Integer.parseInt(get(key));
  }

  public static String credential(String name) {
    String v = System.getenv(name);
    if (v == null || v.isBlank())
      throw new IllegalStateException(
          "Set environment variable " + name + " using the documented public test account");
    return v;
  }

  private Config() {}
}
