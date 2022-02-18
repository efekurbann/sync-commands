package xyz.efekurbann.synccommands.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import xyz.efekurbann.synccommands.SyncCommandsBungee;
import xyz.efekurbann.synccommands.objects.Command;
import xyz.efekurbann.synccommands.objects.Server;
import xyz.efekurbann.synccommands.util.ChatUtils;

import java.util.Arrays;

public class BSyncCommand extends net.md_5.bungee.api.plugin.Command {

    private final SyncCommandsBungee plugin;

    public BSyncCommand(SyncCommandsBungee plugin) {
        super("bsync", null, "bungeesync", "syncbungee");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bsync.op")) {
            sender.sendMessage(TextComponent.fromLegacyText(
                    ChatUtils.color(plugin.getConfig().getString("no-permission"))));
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

            plugin.getMessaging().publishCommand(new Command(command, plugin.getServer(), servers));
        } else if (plugin.getServers().get(args[0]) != null)
            plugin.getMessaging().publishCommand(new Command(command, plugin.getServer(), plugin.getServers().get(args[0])));
        else {
            sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color(String.format("&cCould not find %s!", args[0]))));
            return;
        }

        sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.color("&aSuccessfully sent command.")));
    }
}
