package io.github.efekurbann.synccommands.scheduler;

import io.github.efekurbann.synccommands.SyncCommandsSpigot;
import org.bukkit.Bukkit;

public class BukkitScheduler implements Scheduler {

    private final SyncCommandsSpigot plugin;

    public BukkitScheduler(SyncCommandsSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
