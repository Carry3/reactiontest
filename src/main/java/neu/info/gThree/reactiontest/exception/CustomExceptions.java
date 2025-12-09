package neu.info.gThree.reactiontest.exception;

public class CustomExceptions {

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

}
