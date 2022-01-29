package xyz.efekurbann.synccommands.executor.impl;

import net.md_5.bungee.api.ProxyServer;
import xyz.efekurbann.synccommands.executor.ConsoleExecutor;

public class BungeeExecutor implements ConsoleExecutor {
    @Override
    public void execute(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
