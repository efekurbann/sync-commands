package io.github.efekurbann.synccommands.config;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class Config {

    private Configuration config;
    private final Plugin plugin;
    private final String name;

    public Config(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void create() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File configFile = new File(plugin.getDataFolder(), name);
        if (!configFile.exists()) {
            try (InputStream is = plugin.getResourceAsStream(name);
                 OutputStream os = Files.newOutputStream(configFile.toPath())) {
                configFile.createNewFile();
                ByteStreams.copy(is, os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration getConfig() {
        return config;
    }
}
