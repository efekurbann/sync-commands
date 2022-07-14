package io.github.efekurbann.synccommands.executor.impl;

import io.github.efekurbann.synccommands.SyncCommandsVelocity;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;

public class VelocityExecutor implements ConsoleExecutor {

    private final SyncCommandsVelocity plugin;

    public VelocityExecutor(SyncCommandsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(String command) {
        plugin.getProxyServer().getCommandManager().executeAsync(plugin.getProxyServer().getConsoleCommandSource(), command);
    }
}
