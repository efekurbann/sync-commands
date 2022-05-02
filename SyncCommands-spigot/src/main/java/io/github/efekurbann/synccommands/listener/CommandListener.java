package io.github.efekurbann.synccommands.listener;

import io.github.efekurbann.synccommands.SyncCommandsSpigot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final SyncCommandsSpigot plugin;

    public CommandListener(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().replace("/", "");

        Player player = event.getPlayer();
        if (!plugin.getAutoSyncMode().containsKey(player.getUniqueId())) return;
        if (command.startsWith("bsync") || command.startsWith("bungeesync") || command.startsWith("syncbungee")
                || command.startsWith("sync") || command.startsWith("ssync")) return;

        String target = plugin.getAutoSyncMode().get(player.getUniqueId());

        event.setMessage(String.format("/sync %s %s", target, command));
    }

}
