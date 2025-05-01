package kafka.kafka.exception;

public class FilterEvaluationException extends RuntimeException {
    public FilterEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}