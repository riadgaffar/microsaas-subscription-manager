package skyvangaurd.subscription.error;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
      super(message);
  }
}
