package io.github.efekurbann.synccommands.executor.impl;

import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import net.md_5.bungee.api.ProxyServer;

public class BungeeExecutor implements ConsoleExecutor {
    @Override
    public void execute(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
