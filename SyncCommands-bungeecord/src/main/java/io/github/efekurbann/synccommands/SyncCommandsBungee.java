package io.github.efekurbann.synccommands;

import io.github.efekurbann.synccommands.config.Config;
import io.github.efekurbann.synccommands.executor.impl.BungeeExecutor;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.messaging.impl.redis.Redis;
import io.github.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import io.github.efekurbann.synccommands.objects.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;
import io.github.efekurbann.synccommands.util.UpdateChecker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.bstats.bungeecord.Metrics;
import io.github.efekurbann.synccommands.command.BSyncCommand;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.scheduler.BungeeScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class SyncCommandsBungee extends Plugin {

    private final Config config = new Config(this, "config.yml");
    private final ConsoleExecutor consoleExecutor = new BungeeExecutor();
    private final Scheduler scheduler = new BungeeScheduler(this);
    private final Map<String, Server> servers = new HashMap<>();
    private Server server;
    private Messaging messaging;

    @Override
    public void onEnable() {
        this.config.create();

        this.server = new Server(
                this.getConfig().getString("serverName"),
                this.getConfig().getString("connection.host"),
                this.getConfig().getInt("connection.port"),
                this.getConfig().getString("connection.password"),
                this.getConfig().getBoolean("connection.secure"));

        String type = this.getConfig().getString("connection.type", "socket");
        if (type.equalsIgnoreCase("socket"))
            this.messaging = new SocketImpl(server, consoleExecutor, this.getLogger(), scheduler);
        else if (type.equalsIgnoreCase("redis"))
            this.messaging = new Redis(server, consoleExecutor, this.getLogger(), scheduler);

        this.messaging.connect(
                server.getHost(),
                server.getPort(),
                server.getPassword(),
                server.isSecure()
        );

        this.messaging.addListeners();

        for (String key : getConfig().getSection("servers").getKeys()) {
            Server s = new Server(
                    key,
                    getConfig().getString("servers." + key + ".host"),
                    getConfig().getInt("servers." + key + ".port"),
                    getConfig().getString("servers." + key + ".password"),
                    getConfig().getBoolean("servers." + key + ".secure"));
            this.servers.put(key, s);
        }

        this.getProxy().getPluginManager().registerCommand(this, new BSyncCommand(this));

        new Metrics(this, 14139);

        // some forks sends ugly messages on initialization
        // so we will check updates after 3 seconds
        ProxyServer.getInstance().getScheduler().schedule(this,
                ()-> new UpdateChecker(this.getDescription().getVersion(), this.getLogger(), scheduler).checkUpdates(),
                3, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        this.messaging.close();
    }

    public Configuration getConfig() {
        return config.getConfig();
    }

    public Messaging getMessaging() {
        return messaging;
    }

    public Server getServer() {
        return server;
    }

    public Map<String, Server> getServers() {
        return servers;
    }
}
