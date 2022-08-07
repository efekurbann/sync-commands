package io.github.efekurbann.synccommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.efekurbann.synccommands.command.BSyncCommand;
import io.github.efekurbann.synccommands.config.Config;
import io.github.efekurbann.synccommands.enums.ConnectionType;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.executor.impl.BungeeExecutor;
import io.github.efekurbann.synccommands.listener.CommandListener;
import io.github.efekurbann.synccommands.logging.Logger;
import io.github.efekurbann.synccommands.logging.impl.BungeeLogger;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.messaging.impl.rabbitmq.RabbitMQ;
import io.github.efekurbann.synccommands.messaging.impl.redis.Redis;
import io.github.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import io.github.efekurbann.synccommands.objects.server.MQServer;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;
import io.github.efekurbann.synccommands.scheduler.impl.BungeeScheduler;
import io.github.efekurbann.synccommands.util.UpdateChecker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import org.bstats.bungeecord.Metrics;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class SyncCommandsBungee extends Plugin {

    private final Config config = new Config(this, "config.yml");
    private final ConsoleExecutor consoleExecutor = new BungeeExecutor();
    private final Scheduler scheduler = new BungeeScheduler(this);
    private final Logger logger = new BungeeLogger(this);
    private final Map<String, Server> servers = new HashMap<>();
    private final Map<String, Set<Server>> groups = new HashMap<>();
    private final Cache<UUID, String> autoSyncMode = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private Server server;
    private Messaging messaging;
    private boolean connectedSuccessfully;

    @Override
    public void onEnable() {
        this.config.create();

        this.getLogger().info("Creating connection...");

        ConnectionType type;
        try {
            type = ConnectionType.valueOf(this.getConfig().getString("connection.type", "socket")
                    .toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            this.getLogger().info("Invalid connection type detected! Valid types: Redis, RabbitMQ, Socket");
            this.onDisable();
            return;
        }

        if (!this.connect(type)) {
            onDisable();
            return;
        }

        this.getLogger().info("Setting up servers...");
        setupServers(type);

        this.getLogger().info("Setting up groups...");
        setupGroups();

        this.getProxy().getPluginManager().registerCommand(this, new BSyncCommand(this));
        this.getProxy().getPluginManager().registerListener(this, new CommandListener(this));

        new Metrics(this, 14139);

        // some forks sends ugly messages on initialization
        // so we will check updates after 3 seconds
        ProxyServer.getInstance().getScheduler().schedule(this,
                ()-> new UpdateChecker(this.getDescription().getVersion(), logger, scheduler).checkUpdates(),
                3, TimeUnit.SECONDS);

        this.getLogger().info("Everything seems good. Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (connectedSuccessfully)
            this.messaging.close();
    }

    private boolean connect(ConnectionType type) {
        if (type != ConnectionType.RABBITMQ) {
            this.server = new Server(
                    this.getConfig().getString("serverName"),
                    this.getConfig().getString("connection.host"),
                    this.getConfig().getInt("connection.port"),
                    this.getConfig().getString("connection.password"),
                    this.getConfig().getBoolean("connection.secure"));
        } else {
            this.server = new MQServer(
                    this.getConfig().getString("serverName"),
                    this.getConfig().getString("connection.host"),
                    this.getConfig().getInt("connection.port"),
                    this.getConfig().getString("connection.password"),
                    this.getConfig().getBoolean("connection.secure"),
                    this.getConfig().getString("connection.username"),
                    this.getConfig().getString("connection.vhost"));
        }

        switch (type) {
            case SOCKET:
                this.messaging = new SocketImpl(server, consoleExecutor, logger, scheduler);
                break;
            case REDIS:
                this.messaging = new Redis(server, consoleExecutor, logger, scheduler);
                break;
            case RABBITMQ:
                this.messaging = new RabbitMQ(server, consoleExecutor, logger, scheduler);
                break;
        }

        // this may not be the best solution but I just don't want people to see those ugly exceptions
        try {
            this.messaging.connect(
                    server.getHost(),
                    server.getPort(),
                    server.getPassword(),
                    server.isSecure()
            );

            this.getLogger().info("Setting up message listeners...");

            if (type != ConnectionType.RABBITMQ) // no need to call the method twice
                this.messaging.addListeners();

            this.connectedSuccessfully = true;
        } catch (Exception ex) {
            this.getLogger().info("Something went wrong! We could not setup a connection!");
            this.getLogger().info("Please fix your configuration! Plugin disabling...");
            this.getLogger().info("Exception message: " + ex.getMessage());
            this.connectedSuccessfully = false;
        }

        return this.connectedSuccessfully;
    }

    private void setupServers(ConnectionType type) {
        if (getConfig().getSection("servers") == null) return;

        for (String key : getConfig().getSection("servers").getKeys()) {
            Server s;
            if (type != ConnectionType.RABBITMQ) {
                s = new Server(
                        key,
                        getConfig().getString("servers." + key + ".host"),
                        getConfig().getInt("servers." + key + ".port"),
                        getConfig().getString("servers." + key + ".password"),
                        getConfig().getBoolean("servers." + key + ".secure"));
            } else {
                s = new MQServer(
                        key,
                        getConfig().getString("servers." + key + ".host"),
                        getConfig().getInt("servers." + key + ".port"),
                        getConfig().getString("servers." + key + ".password"),
                        getConfig().getBoolean("servers." + key + ".secure"),
                        getConfig().getString("servers." + key + ".username"),
                        getConfig().getString("servers." + key + ".vhost"));
            }
            this.servers.put(key, s);
        }

        this.servers.put(this.server.getServerName(), this.server);
    }

    private void setupGroups() {
        if (getConfig().getSection("groups") == null) return;

        for (String key : getConfig().getSection("groups").getKeys()) {
            Set<Server> servers = new HashSet<>();

            for (String server : getConfig().getStringList("groups." + key)) {
                servers.add(this.servers.get(server));
            }

            this.groups.put(key, servers);
        }
    }

    public Configuration getConfig() {
        return config.getConfig();
    }

    public Messaging getMessaging() {
        return messaging;
    }

    public Server getThisServer() {
        return server;
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    public Map<String, Set<Server>> getGroups() {
        return groups;
    }

    public Cache<UUID, String> getAutoSyncMode() {
        return autoSyncMode;
    }
}
