package io.github.efekurbann.synccommands.scheduler.impl;

import io.github.efekurbann.synccommands.SyncCommandsBungee;
import io.github.efekurbann.synccommands.scheduler.Scheduler;
import net.md_5.bungee.api.ProxyServer;

public class BungeeScheduler implements Scheduler {

    private final SyncCommandsBungee plugin;

    public BungeeScheduler(SyncCommandsBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runSync(Runnable runnable) {
        // since there is no "main thread" stuff in bungee, we are going to run it async
        ProxyServer.getInstance().getScheduler().runAsync(plugin, runnable);
    }
}
