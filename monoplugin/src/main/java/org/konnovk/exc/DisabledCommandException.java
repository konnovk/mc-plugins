package org.konnovk.exc;

public class DisabledCommandException extends RuntimeException {
    private static final String MESSAGE = "Команда отключена на сервере";

    public DisabledCommandException() {
        super(MESSAGE);
    }
}
