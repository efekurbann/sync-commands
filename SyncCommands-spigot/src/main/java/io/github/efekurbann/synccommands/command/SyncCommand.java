package io.github.efekurbann.synccommands.command;

import io.github.efekurbann.synccommands.objects.server.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        if (!sender.hasPermission("synccommands.admin")) {
            sender.sendMessage(ChatUtils.color(plugin.getConfig().getString("no-permission")));
            return true;
        }

        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            String target = args[0];
            if (!plugin.getGroups().containsKey(target)
                    && !plugin.getServers().containsKey(target)
                    && !target.equalsIgnoreCase("all")) {
                sender.sendMessage(ChatUtils.color(String.format("&cCould not find %s!", target)));
                return true;
            }

            if (!plugin.getAutoSyncMode().containsKey(player.getUniqueId())) {
                plugin.getAutoSyncMode().put(player.getUniqueId(), target);
                sender.sendMessage(
                        ChatUtils.color(String.format("&aSuccessfully enabled the auto sync mode for %s!", target))
                );
                sender.sendMessage(
                        ChatUtils.color("&aFrom now on, all the commands that you execute will be synced.")
                );
                sender.sendMessage(
                        ChatUtils.color("&6&lNOTE! &eSince the proxies are hierarchically higher than spigot," +
                                " your command will be proceed on your proxy first.")
                );
                sender.sendMessage(
                        ChatUtils.color("&eThat means if you use a bungeecord command like /alert, " +
                                "we can not detect that. And it wont be synced.")
                );
            } else {
                plugin.getAutoSyncMode().remove(player.getUniqueId());
                sender.sendMessage(ChatUtils.color("&cDisabled the auto sync mode!"));
            }

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
        else if (plugin.getGroups().get(args[0]) != null) {
            Server[] servers = plugin.getGroups().get(args[0]).toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(cmd, plugin.getThisServer(), servers));
        } else {
            sender.sendMessage(ChatUtils.color(String.format("&cCould not find %s!", args[0])));
            return true;
        }

        sender.sendMessage(ChatUtils.color("&aSuccessfully sent command."));
        return true;
    }
}
