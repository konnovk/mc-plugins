package org.konnovk.exc;

public class TpaRequestsNotFoundException extends CommandException {
    private static final String MESSAGE = "Запросы на телепортацию не найдены";

    public TpaRequestsNotFoundException() {
        super(MESSAGE);
    }
}
