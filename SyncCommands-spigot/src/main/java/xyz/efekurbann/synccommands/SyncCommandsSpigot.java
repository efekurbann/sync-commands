package xyz.efekurbann.synccommands;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.efekurbann.synccommands.command.SyncCommand;
import xyz.efekurbann.synccommands.config.Config;
import xyz.efekurbann.synccommands.executor.ConsoleExecutor;
import xyz.efekurbann.synccommands.executor.impl.BukkitExecutor;
import xyz.efekurbann.synccommands.messaging.Messaging;
import xyz.efekurbann.synccommands.messaging.impl.redis.Redis;
import xyz.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import xyz.efekurbann.synccommands.objects.Server;
import xyz.efekurbann.synccommands.scheduler.BukkitScheduler;
import xyz.efekurbann.synccommands.scheduler.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class SyncCommandsSpigot extends JavaPlugin {

    private final Config config = new Config(this, "config.yml");
    private final ConsoleExecutor consoleExecutor = new BukkitExecutor(this);
    private final Map<String, Server> servers = new HashMap<>();
    private final Scheduler scheduler = new BukkitScheduler(this);
    private Messaging messaging;
    private Server server;

    @Override
    public void onEnable() {
        config.create();

        this.server = new Server(
                this.getConfig().getString("serverName"),
                this.getConfig().getString("connection.host"),
                this.getConfig().getInt("connection.port"),
                this.getConfig().getString("connection.password"),
                this.getConfig().getBoolean("connection.secure"));

        if (this.getConfig().getString("connection.type").equalsIgnoreCase("socket"))
            this.messaging = new SocketImpl(server, consoleExecutor, this.getLogger(), scheduler);
        else if (this.getConfig().getString("connection.type").equalsIgnoreCase("redis"))
            this.messaging = new Redis(server, consoleExecutor, this.getLogger(), scheduler);

        this.messaging.connect(
                server.getHost(),
                server.getPort(),
                server.getPassword(),
                server.isSecure()
        );

        this.messaging.addListeners();

        for (String key : getConfig().getConfigurationSection("servers").getKeys(false)) {
            Server s = new Server(
                    key,
                    getConfig().getString("servers." + key + ".host"),
                    getConfig().getInt("servers." + key + ".port"),
                    getConfig().getString("servers." + key + ".password"),
                    getConfig().getBoolean("servers." + key + ".secure"));
            this.servers.put(key, s);
        }

        this.getCommand("sync").setExecutor(new SyncCommand(this));
    }

    @Override
    public void onDisable() {
        this.messaging.close();
    }

    @NotNull
    @Override
    public Config getConfig() {
        return config;
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    @NotNull
    public Server getThisServer() {
        return server;
    }

    public Messaging getMessaging() {
        return messaging;
    }
}