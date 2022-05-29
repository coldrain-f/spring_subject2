package edu.coldrain.spring_subject1.exception;

public class DuplicateBoardException extends RuntimeException {
    public DuplicateBoardException() {
        super();
    }
    public DuplicateBoardException(String message, Throwable cause) {
        super(message, cause);
    }
    public DuplicateBoardException(String message) {
        super(message);
    }
    public DuplicateBoardException(Throwable cause) {
        super(cause);
    }
}
