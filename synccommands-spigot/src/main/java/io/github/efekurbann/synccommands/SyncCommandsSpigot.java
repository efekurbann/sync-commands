package io.github.efekurbann.synccommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.efekurbann.synccommands.command.SyncCommand;
import io.github.efekurbann.synccommands.config.Config;
import io.github.efekurbann.synccommands.enums.ConnectionType;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.executor.impl.BukkitExecutor;
import io.github.efekurbann.synccommands.listener.CommandListener;
import io.github.efekurbann.synccommands.logging.BukkitLogger;
import io.github.efekurbann.synccommands.logging.Logger;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.messaging.impl.rabbitmq.RabbitMQ;
import io.github.efekurbann.synccommands.messaging.impl.redis.Redis;
import io.github.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import io.github.efekurbann.synccommands.objects.server.MQServer;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;
import io.github.efekurbann.synccommands.scheduler.impl.BukkitScheduler;
import io.github.efekurbann.synccommands.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class SyncCommandsSpigot extends JavaPlugin {

    private final Config config = new Config(this, "config.yml");
    private final ConsoleExecutor consoleExecutor = new BukkitExecutor(this);
    private final Map<String, Server> servers = new HashMap<>();
    private final Map<String, Set<Server>> groups = new HashMap<>();
    // we will store the names instead of uuids
    private final Cache<String, String> autoSyncMode = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final Scheduler scheduler = new BukkitScheduler(this);
    private final Logger logger = new BukkitLogger(this);
    private UpdateChecker updateChecker;
    private Messaging messaging;
    private Server server;
    private boolean connectedSuccessfully;

    @Override
    public void onEnable() {
        config.create();

        this.getLogger().info("Creating connection...");

        ConnectionType type;
        try {
            type = ConnectionType.valueOf(this.getConfig().getString("connection.type", "socket")
                    .toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            this.getLogger().info("Invalid connection type detected! Valid types: Redis, RabbitMQ, Socket");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!this.connect(type)) return;

        this.getLogger().info("Setting up servers...");
        setupServers(type);

        this.getLogger().info("Setting up groups...");
        setupGroups();

        this.getCommand("sync").setExecutor(new SyncCommand(this));
        this.getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        new Metrics(this, 14138);

        (updateChecker = new UpdateChecker(this.getDescription().getVersion(), logger, scheduler)).checkUpdates();

        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();

                if (!player.hasPermission("synccommands.admin")) return;

                if (!updateChecker.isUpToDate()) {
                    player.sendMessage(ChatColor.GOLD + "[SyncCommands]" + ChatColor.YELLOW + " An update was found!");
                    player.sendMessage(ChatColor.GOLD + "[SyncCommands]" + ChatColor.YELLOW +
                            " Download from: https://www.spigotmc.org/resources/99596");
                }
            }
        }, this);

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

        // this may not be the best solution, but I just don't want people to see those ugly exceptions
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
            this.getServer().getPluginManager().disablePlugin(this);
            this.connectedSuccessfully = false;
        }

        return this.connectedSuccessfully;
    }

    private void setupServers(ConnectionType type) {
        if (this.getConfig().getConfigurationSection("servers") == null) return;

        for (String key : getConfig().getConfigurationSection("servers").getKeys(false)) {
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
        if (getConfig().getConfigurationSection("groups") == null) return;

        for (String key : getConfig().getConfigurationSection("groups").getKeys(false)) {
            Set<Server> servers = new HashSet<>();

            for (String server : getConfig().getStringList("groups." + key)) {
                servers.add(this.servers.get(server));
            }

            this.groups.put(key, servers);
        }
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Map<String, Set<Server>> getGroups() {
        return groups;
    }

    public Cache<String, String> getAutoSyncMode() {
        return autoSyncMode;
    }
}
