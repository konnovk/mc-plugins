package org.konnovk.exc;

public class HomeNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Дом %s не найден";

    public HomeNotFoundException(String homeName) {
        super(MESSAGE.formatted(homeName));
    }
}
