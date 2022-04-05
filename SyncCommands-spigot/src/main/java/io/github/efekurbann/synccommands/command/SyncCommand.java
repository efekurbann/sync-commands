package io.github.efekurbann.synccommands.command;

import io.github.efekurbann.synccommands.objects.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import io.github.efekurbann.synccommands.SyncCommandsSpigot;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.util.ChatUtils;

import java.util.Arrays;

public class SyncCommand implements CommandExecutor {

    private final SyncCommandsSpigot plugin;

    public SyncCommand(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bsync.op")) {
            sender.sendMessage(ChatUtils.color(plugin.getConfig().getString("no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatUtils.color("&cCorrect usage: /sync <server/all> <command>"));
            return true;
        }

        String cmd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (args[0].equalsIgnoreCase("all")) {
            Server[] servers = plugin.getServers().values().toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(cmd, plugin.getThisServer(), servers));
        } else if (plugin.getServers().get(args[0]) != null)
            plugin.getMessaging().publishCommand(new Command(cmd, plugin.getThisServer(), plugin.getServers().get(args[0])));
        else {
            sender.sendMessage(ChatUtils.color(String.format("&cCould not find %s!", args[0])));
            return true;
        }

        sender.sendMessage(ChatUtils.color("&aSuccessfully sent command."));
        return true;
    }
}
