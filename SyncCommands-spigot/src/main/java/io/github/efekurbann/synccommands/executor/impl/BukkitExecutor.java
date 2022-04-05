package io.github.efekurbann.synccommands.executor.impl;

import io.github.efekurbann.synccommands.SyncCommandsSpigot;
import org.bukkit.Bukkit;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;

public class BukkitExecutor implements ConsoleExecutor {

    private final SyncCommandsSpigot plugin;

    public BukkitExecutor(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        plugin.getScheduler().runSync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
