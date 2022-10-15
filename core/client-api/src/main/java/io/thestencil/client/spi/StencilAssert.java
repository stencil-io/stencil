package io.thestencil.client.spi;


import java.util.function.Supplier;

public class StencilAssert {
  public static class StencilAssertException extends IllegalArgumentException {
    private static final long serialVersionUID = 6305063707279384796L;
    public StencilAssertException(String s) {
      super(s);
    }
  }

  
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new StencilAssertException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new StencilAssertException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new StencilAssertException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
