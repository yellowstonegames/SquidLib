package squidpony.squidgrid.mapping.locks.util;

public class GenerationFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GenerationFailureException(String message) {
        super(message);
    }

    public GenerationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
