package io.github.efekurbann.synccommands.listener;

import io.github.efekurbann.synccommands.SyncCommandsSpigot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {

    private final SyncCommandsSpigot plugin;

    public CommandListener(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().replace("/", "");

        Player player = event.getPlayer();
        if (!plugin.getAutoSyncMode().asMap().containsKey(player.getName())) return;
        if (command.startsWith("bsync") || command.startsWith("bungeesync") || command.startsWith("syncbungee")
                || command.startsWith("sync") || command.startsWith("ssync")) return;

        String target = plugin.getAutoSyncMode().getIfPresent(player.getName());

        event.setMessage(String.format("/sync %s %s", target, command));
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        String command = event.getCommand().replace("/", "");

        CommandSender sender = event.getSender();
        if (!plugin.getAutoSyncMode().asMap().containsKey(sender.getName())) return;
        if (command.startsWith("bsync") || command.startsWith("bungeesync") || command.startsWith("syncbungee")
                || command.startsWith("sync") || command.startsWith("ssync")) return;

        String target = plugin.getAutoSyncMode().getIfPresent(sender.getName());

        event.setCommand(String.format("sync %s %s", target, command));
    }

}
