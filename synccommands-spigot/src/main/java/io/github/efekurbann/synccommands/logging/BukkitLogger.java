package io.github.efekurbann.synccommands.logging;

import io.github.efekurbann.synccommands.SyncCommandsSpigot;

public class BukkitLogger implements Logger {

    private final SyncCommandsSpigot plugin;

    public BukkitLogger(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void severe(String message) {
        plugin.getLogger().severe(message);
    }
}
