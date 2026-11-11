package org.konnovk.exc;

public class DisabledCommandException extends CommandException {
    private static final String MESSAGE = "Команда отключена на сервере";

    public DisabledCommandException() {
        super(MESSAGE);
    }
}
