package org.konnovk.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.konnovk.KCommand;
import org.konnovk.auth.AuthService;
import org.konnovk.exc.DisabledCommandException;
import org.konnovk.exc.IncorrectArgumentsException;

import java.util.Arrays;

@RequiredArgsConstructor
public class LoginCommand implements KCommand {
    private final boolean enabled;
    private final AuthService authService;

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!enabled) {
            throw new DisabledCommandException();
        }

        if (args.length != 1 || !(sender instanceof Player player)) {
            throw new IncorrectArgumentsException();
        }

        if (!authService.isRegistered(player)) {
            player.sendMessage(Component.text("Вы ещё не зарегистрированы. Используйте /register <пароль>.").color(NamedTextColor.RED));
            return true;
        }

        if (authService.isAuthenticated(player)) {
            player.sendMessage(Component.text("Вы уже авторизованы.").color(NamedTextColor.GREEN));
            return true;
        }

        char[] password = args[0].toCharArray();
        boolean authenticated;
        try {
            authenticated = authService.login(player, password);
        } finally {
            Arrays.fill(password, '\0');
        }

        if (!authenticated) {
            player.sendMessage(Component.text("Неверный пароль.").color(NamedTextColor.RED));
            return true;
        }

        player.sendMessage(Component.text("Авторизация успешно завершена!").color(NamedTextColor.GREEN));
        return true;
    }
}
