package org.konnovk.exc;

public class HomesLimitExceededException extends RuntimeException {
    private static final String MESSAGE = "Достигнут лимит на количество домов: %d";

    public HomesLimitExceededException(long limit) {
        super(MESSAGE.formatted(limit));
    }
}
