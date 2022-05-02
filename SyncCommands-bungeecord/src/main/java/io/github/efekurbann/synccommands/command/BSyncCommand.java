package io.github.efekurbann.synccommands.command;

import io.github.efekurbann.synccommands.SyncCommandsBungee;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.util.ChatUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;

public class BSyncCommand extends net.md_5.bungee.api.plugin.Command {

    private final SyncCommandsBungee plugin;

    public BSyncCommand(SyncCommandsBungee plugin) {
        super("bsync", null, "bungeesync", "syncbungee");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bsynccommands.admin")) {
            sender.sendMessage(TextComponent.fromLegacyText(
                    ChatUtils.color(plugin.getConfig().getString("no-permission"))));
            return;
        }

        if (args.length == 1 && !ProxyServer.getInstance().getConsole().equals(sender)) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            String target = args[0];
            if (!plugin.getGroups().containsKey(target)
                    && !plugin.getServers().containsKey(target)
                    && !target.equalsIgnoreCase("all")) {
                sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color(String.format("&cCould not find %s!", target))));
                return;
            }

            if (!plugin.getAutoSyncMode().asMap().containsKey(player.getUniqueId())) {
                plugin.getAutoSyncMode().put(player.getUniqueId(), target);
                sender.sendMessage(TextComponent.fromLegacyText(
                        ChatUtils.color(String.format("&aSuccessfully enabled the auto sync mode for %s!", target)))
                );
                sender.sendMessage(TextComponent.fromLegacyText(
                        ChatUtils.color("&aFrom now on, all the commands that you execute will be synced."))
                );
            } else {
                plugin.getAutoSyncMode().invalidate(player.getUniqueId());
                sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color("&cDisabled the auto sync mode!")));
            }

            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText(
                    ChatUtils.color("&cCorrect usage: /bsync <server/all> <command>")));
            return;
        }

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (args[0].equalsIgnoreCase("all")) {
            Server[] servers = plugin.getServers().values().toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(command, plugin.getThisServer(), servers));
        } else if (plugin.getServers().get(args[0]) != null)
            plugin.getMessaging().publishCommand(new Command(command, plugin.getThisServer(), plugin.getServers().get(args[0])));
        else if (plugin.getGroups().get(args[0]) != null) {
            Server[] servers = plugin.getGroups().get(args[0]).toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(command, plugin.getThisServer(), servers));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color(String.format("&cCould not find %s!", args[0]))));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color("&aSuccessfully sent command.")));
    }
}
