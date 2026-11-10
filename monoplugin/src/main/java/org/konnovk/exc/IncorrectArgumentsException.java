package org.konnovk.exc;

public class IncorrectArgumentsException extends RuntimeException {
    private static final String MESSAGE = "Некорректные аргументы команды";

    public IncorrectArgumentsException() {
        super(MESSAGE);
    }
}
