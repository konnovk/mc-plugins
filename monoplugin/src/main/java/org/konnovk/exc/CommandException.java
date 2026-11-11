package org.konnovk.exc;

public class CommandException extends RuntimeException {
    protected CommandException(String message) {
        super(message);
    }
}
