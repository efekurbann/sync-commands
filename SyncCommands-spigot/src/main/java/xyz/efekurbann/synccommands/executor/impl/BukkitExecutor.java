package xyz.efekurbann.synccommands.executor.impl;

import org.bukkit.Bukkit;
import xyz.efekurbann.synccommands.SyncCommandsSpigot;
import xyz.efekurbann.synccommands.executor.ConsoleExecutor;

public class BukkitExecutor implements ConsoleExecutor {

    private final SyncCommandsSpigot plugin;

    public BukkitExecutor(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
