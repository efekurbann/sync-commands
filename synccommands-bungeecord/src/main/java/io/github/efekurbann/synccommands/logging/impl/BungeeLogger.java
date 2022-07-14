package io.github.efekurbann.synccommands.logging.impl;

import io.github.efekurbann.synccommands.SyncCommandsBungee;
import io.github.efekurbann.synccommands.logging.Logger;

public class BungeeLogger implements Logger {

    private final SyncCommandsBungee plugin;

    public BungeeLogger(SyncCommandsBungee plugin) {
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
