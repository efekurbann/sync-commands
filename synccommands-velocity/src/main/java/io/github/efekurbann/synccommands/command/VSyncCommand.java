package io.github.efekurbann.synccommands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import io.github.efekurbann.synccommands.SyncCommandsVelocity;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.Server;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;

public class VSyncCommand implements SimpleCommand {

    private final SyncCommandsVelocity plugin;

    public VSyncCommand(SyncCommandsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!invocation.source().hasPermission("synccommands.proxy.admin")) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    plugin.getConfig().getRoot().getNode("no-permission").getString()
            ).decoration(TextDecoration.ITALIC, false));
            return;
        }

        if (args.length == 1 && !plugin.getProxyServer().getConsoleCommandSource().equals(sender)) {
            String target = args[0];
            if (!plugin.getGroups().containsKey(target)
                    && !plugin.getServers().containsKey(target)
                    && !target.equalsIgnoreCase("all")) {
                sender.sendMessage(Component.text(String.format("Could not find %s!", target)).color(NamedTextColor.RED));
                return;
            }

            Player player = (Player) sender;
            if (!plugin.getAutoSyncMode().asMap().containsKey(player.getUniqueId())) {
                plugin.getAutoSyncMode().put(player.getUniqueId(), target);

                sender.sendMessage(Component.text(
                        String.format("Successfully enabled the auto sync mode for %s!", target)
                ).color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text(
                        "From now on, all the commands that you execute will be synced."
                ).color(NamedTextColor.GREEN));
            } else {
                plugin.getAutoSyncMode().invalidate(player.getUniqueId());
                sender.sendMessage(Component.text("Disabled the auto sync mode!").color(NamedTextColor.RED));
            }

            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Correct usage: /vsync <server/all> <command>").color(NamedTextColor.RED));
            return;
        }

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (args[0].equalsIgnoreCase("all")) {
            Server[] servers = plugin.getServers().values().toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(command, plugin.getServer(), servers));
        } else if (plugin.getServers().get(args[0]) != null)
            plugin.getMessaging().publishCommand(new Command(command, plugin.getServer(), plugin.getServers().get(args[0])));
        else if (plugin.getGroups().get(args[0]) != null) {
            Server[] servers = plugin.getGroups().get(args[0]).toArray(new Server[0]);

            plugin.getMessaging().publishCommand(new Command(command, plugin.getServer(), servers));
        } else {
            sender.sendMessage(Component.text(String.format("Could not find %s!", args[0])).color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Successfully sent command.").color(NamedTextColor.GREEN));
    }

}
