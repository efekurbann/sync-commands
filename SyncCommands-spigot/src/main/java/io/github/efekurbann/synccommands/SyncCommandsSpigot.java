package io.github.efekurbann.synccommands;

import io.github.efekurbann.synccommands.command.SyncCommand;
import io.github.efekurbann.synccommands.config.Config;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.messaging.impl.redis.Redis;
import io.github.efekurbann.synccommands.messaging.impl.socket.SocketImpl;
import io.github.efekurbann.synccommands.objects.Server;
import io.github.efekurbann.synccommands.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.executor.impl.BukkitExecutor;
import io.github.efekurbann.synccommands.scheduler.BukkitScheduler;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.util.HashMap;
import java.util.Map;

public final class SyncCommandsSpigot extends JavaPlugin {

    private final Config config = new Config(this, "config.yml");
    private final ConsoleExecutor consoleExecutor = new BukkitExecutor(this);
    private final Map<String, Server> servers = new HashMap<>();
    private final Scheduler scheduler = new BukkitScheduler(this);
    private UpdateChecker updateChecker;
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

        new Metrics(this, 14138);

        (updateChecker = new UpdateChecker(this.getDescription().getVersion(), this.getLogger(), scheduler)).checkUpdates();

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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
