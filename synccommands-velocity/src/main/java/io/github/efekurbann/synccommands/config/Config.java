package io.github.efekurbann.synccommands.config;

import com.google.common.io.ByteStreams;
import io.github.efekurbann.synccommands.SyncCommandsVelocity;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private final YAMLConfigurationLoader loader;
    private final SyncCommandsVelocity plugin;

    private ConfigurationNode root;

    public Config(SyncCommandsVelocity plugin, Path path) {
        this.plugin = plugin;
        this.loader = YAMLConfigurationLoader.builder().setPath(path).build();
    }

    public void create() {
        if (!plugin.getDataDirectory().toFile().exists()) {
            plugin.getDataDirectory().toFile().mkdirs();
        }

        File configFile = new File(plugin.getDataDirectory().toFile(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream is = plugin.getClass().getClassLoader().getResourceAsStream("config.yml");
                 OutputStream os = Files.newOutputStream(configFile.toPath())) {
                configFile.createNewFile();
                ByteStreams.copy(is, os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.root = loader.load();
        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred while loading this configuration!");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            loader.save(root);
        } catch (final IOException e) {
            plugin.getLogger().severe("Unable to save your messages configuration! Sorry! " + e.getMessage());
        }
    }

    public ConfigurationNode getRoot() {
        return root;
    }
}
