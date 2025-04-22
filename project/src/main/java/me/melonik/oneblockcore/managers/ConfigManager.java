package me.melonik.oneblockcore.managers;

import me.melonik.oneblockcore.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
    }

    private void setDefaults() {
        config.addDefault("island.spacing", 800);
        config.addDefault("island.height", 64);
        config.addDefault("island.initial-border", 100);
        config.addDefault("island.max-members", 5);

        config.addDefault("generator.base-progress", 10);
        config.addDefault("generator.level-multiplier", 0.5);

        config.addDefault("economy.death-penalty", 0.1);
        config.addDefault("economy.initial-money", 0.0);
        config.addDefault("economy.initial-bank", 0.0);

        config.addDefault("border.base-cost", 1000.0);
        config.addDefault("border.cost-multiplier", 2.0);

        config.options().copyDefaults(true);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać konfiguracji: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public int getIslandSpacing() {
        return config.getInt("island.spacing", 800);
    }

    public int getIslandHeight() {
        return config.getInt("island.height", 64);
    }

    public int getInitialBorder() {
        return config.getInt("island.initial-border", 100);
    }

    public int getMaxMembers() {
        return config.getInt("island.max-members", 5);
    }

    public double getBaseProgress() {
        return config.getDouble("generator.base-progress", 10.0);
    }

    public double getLevelMultiplier() {
        return config.getDouble("generator.level-multiplier", 0.5);
    }

    public double getDeathPenalty() {
        return config.getDouble("economy.death-penalty", 0.1);
    }

    public double getInitialMoney() {
        return config.getDouble("economy.initial-money", 0.0);
    }

    public double getInitialBank() {
        return config.getDouble("economy.initial-bank", 0.0);
    }

    public double getBorderBaseCost() {
        return config.getDouble("border.base-cost", 1000.0);
    }

    public double getBorderCostMultiplier() {
        return config.getDouble("border.cost-multiplier", 2.0);
    }
}