package io.github.efekurbann.synccommands.scheduler.impl;

import io.github.efekurbann.synccommands.SyncCommandsVelocity;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

public class VelocityScheduler implements Scheduler {

    private final SyncCommandsVelocity plugin;

    public VelocityScheduler(SyncCommandsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable runnable) {
        plugin.getProxyServer().getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public void runSync(Runnable runnable) {
        plugin.getProxyServer().getScheduler().buildTask(plugin, runnable).schedule();
    }
}
