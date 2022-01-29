package xyz.efekurbann.synccommands.scheduler;

import org.bukkit.Bukkit;
import xyz.efekurbann.synccommands.SyncCommandsSpigot;

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
