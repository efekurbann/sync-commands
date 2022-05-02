package io.github.efekurbann.synccommands.listener;

import io.github.efekurbann.synccommands.SyncCommandsBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {

    private final SyncCommandsBungee plugin;

    public CommandListener(SyncCommandsBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(ChatEvent event) {
        if (!event.isCommand()) return;
        String command = event.getMessage().replace("/", "");

        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
        if (!plugin.getAutoSyncMode().asMap().containsKey(sender.getUniqueId())) return;
        if (command.startsWith("bsync") || command.startsWith("bungeesync") || command.startsWith("syncbungee")) return;

        String target = plugin.getAutoSyncMode().getIfPresent(sender.getUniqueId());

        event.setMessage(String.format("/bsync %s %s", target, command));
    }

}
