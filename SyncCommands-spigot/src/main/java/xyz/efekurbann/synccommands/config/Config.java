package xyz.efekurbann.synccommands.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class Config extends YamlConfiguration {

    private final File file;
    private final Plugin plugin;

    public Config(Plugin plugin, String name){
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), name);

        if (file.exists())
            reload();
    }

    public void create(){
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }
        reload();
    }

    public void reload() {
        try {
            this.load(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
