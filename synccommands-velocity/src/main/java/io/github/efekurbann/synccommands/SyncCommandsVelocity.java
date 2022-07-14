package io.github.efekurbann.synccommands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.efekurbann.synccommands.command.VSyncCommand;
import io.github.efekurbann.synccommands.config.Config;
import io.github.efekurbann.synccommands.enums.ConnectionType;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.executor.impl.VelocityExecutor;
import io.github.efekurbann.synccommands.logging.Logger;
import io.github.efekurbann.synccommands.logging.impl.VelocityLogger;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.messaging.impl.rabbitmq.RabbitMQ;
import io.github.efekurbann.synccommands.messaging.impl.redis.Redis;
import io.github.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import io.github.efekurbann.synccommands.objects.server.MQServer;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;
import io.github.efekurbann.synccommands.scheduler.impl.VelocityScheduler;
import io.github.efekurbann.synccommands.util.UpdateChecker;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.velocity.Metrics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "synccommands",
        name = "SyncCommands",
        version = "3.1",
        url = "https://efekurbann.github.io",
        description = "Best Command Synchronization Plugin in the market.",
        authors = {"hyperion"}
)
public class SyncCommandsVelocity {

    private final ProxyServer proxyServer;
    private final org.slf4j.Logger pluginLogger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;
    private final PluginDescription description;
    private final ConsoleExecutor consoleExecutor = new VelocityExecutor(this);
    private final Map<String, Server> servers = new HashMap<>();
    private final Map<String, Set<Server>> groups = new HashMap<>();
    private final Scheduler scheduler = new VelocityScheduler(this);
    private final Cache<UUID, String> autoSyncMode = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    private Messaging messaging;
    private Server server;
    private boolean connectedSuccessfully;
    private Logger logger;

    private Config config;

    @Inject
    public SyncCommandsVelocity(ProxyServer server, org.slf4j.Logger logger, @DataDirectory Path dataDirectory,
                                Metrics.Factory metricsFactory, PluginDescription description) {
        this.proxyServer = server;
        this.pluginLogger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
        this.description = description;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config = new Config(this, Paths.get(dataDirectory.toString(), "config.yml"));
        config.create();

        this.logger = new VelocityLogger(pluginLogger);

        this.getLogger().info("Creating connection...");

        ConnectionType type;
        try {
            type = ConnectionType.valueOf(this.getConfig().getRoot().getNode("connection", "type")
                    .getString().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            this.getLogger().info("Invalid connection type detected! Valid types: Redis, RabbitMQ, Socket");
            return;
        }

        if (!this.connect(type)) return;

        this.getLogger().info("Setting up servers...");
        setupServers(type);

        this.getLogger().info("Setting up groups...");
        setupGroups();

        metricsFactory.make(this, 15759);

        new UpdateChecker(description.getVersion().orElse("unknown"), logger, scheduler).checkUpdates();

        CommandMeta meta = proxyServer.getCommandManager().metaBuilder("vsync").build();
        proxyServer.getCommandManager().register(meta, new VSyncCommand(this));

        this.getLogger().info("Everything seems good. Plugin enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (connectedSuccessfully)
            this.messaging.close();
    }

    private boolean connect(ConnectionType type) {
        if (type != ConnectionType.RABBITMQ) {
            this.server = new Server(
                    this.getConfig().getRoot().getNode("serverName").getString(),
                    this.getConfig().getRoot().getNode("connection", "host").getString(),
                    this.getConfig().getRoot().getNode("connection", "port").getInt(),
                    this.getConfig().getRoot().getNode("connection", "password").getString(),
                    this.getConfig().getRoot().getNode("connection", "secure").getBoolean());
        } else {
            this.server = new MQServer(
                    this.getConfig().getRoot().getNode("serverName").getString(),
                    this.getConfig().getRoot().getNode("connection", "host").getString(),
                    this.getConfig().getRoot().getNode("connection", "port").getInt(),
                    this.getConfig().getRoot().getNode("connection", "password").getString(),
                    this.getConfig().getRoot().getNode("connection", "secure").getBoolean(),
                    this.getConfig().getRoot().getNode("connection", "username").getString(),
                    this.getConfig().getRoot().getNode("connection", "vhost").getString());
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
            this.connectedSuccessfully = false;
        }

        return this.connectedSuccessfully;
    }

    private void setupServers(ConnectionType type) {
        for (Map.Entry<Object, ? extends ConfigurationNode> node :
                this.getConfig().getRoot().getNode("servers").getChildrenMap().entrySet()) {
            Server s;
            if (type != ConnectionType.RABBITMQ) {
                s = new Server(
                        (String) node.getKey(),
                        node.getValue().getNode("host").getString(),
                        node.getValue().getNode("port").getInt(),
                        node.getValue().getNode("password").getString(),
                        node.getValue().getNode("secure").getBoolean());
            } else {
                s = new MQServer(
                        (String) node.getKey(),
                        node.getValue().getNode("host").getString(),
                        node.getValue().getNode("port").getInt(),
                        node.getValue().getNode("password").getString(),
                        node.getValue().getNode("secure").getBoolean(),
                        node.getValue().getNode("username").getString(),
                        node.getValue().getNode("vhost").getString());
            }
            this.servers.put((String) node.getKey(), s);
        }
    }

    private void setupGroups() {
        for (Map.Entry<Object, ? extends ConfigurationNode> node :
                this.getConfig().getRoot().getNode("groups").getChildrenMap().entrySet()) {
            Set<Server> servers = new HashSet<>();

            try {
                for (String s : node.getValue().getList(TypeToken.of(String.class))) {
                    servers.add(this.servers.get(s));
                }
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }

            this.groups.put((String) node.getKey(), servers);
        }
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    public Cache<UUID, String> getAutoSyncMode() {
        return autoSyncMode;
    }

    public Map<String, Set<Server>> getGroups() {
        return groups;
    }

    public Server getServer() {
        return server;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Config getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Messaging getMessaging() {
        return messaging;
    }
}
