package me.melonik.oneblockcore;

import me.clip.placeholderapi.PlaceholderAPI;
import me.melonik.oneblockcore.commands.*;
import me.melonik.oneblockcore.listeners.*;
import me.melonik.oneblockcore.managers.*;
import me.melonik.oneblockcore.models.Island;
import me.melonik.oneblockcore.placeholders.OneBlockPlaceholders;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private IslandManager islandManager;
    private EconomyManager economyManager;
    private GeneratorManager generatorManager;
    private SpawnerManager spawnerManager;
    private Economy economy;
    private boolean vaultEnabled = false;

    @Override
    public void onEnable() {
        startMoneyGeneratorTask();
        startScrafRemovalTask();

        if (setupEconomy()) {
            getLogger().info("Vault economy found and hooked successfully!");
            vaultEnabled = true;
        } else {
            getLogger().severe("Vault economy not found! Make sure you have an economy plugin installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.islandManager = new IslandManager(this);
        this.economyManager = new EconomyManager(this);
        this.generatorManager = new GeneratorManager(this);
        this.spawnerManager = new SpawnerManager(this);

        databaseManager.loadData();

        getCommand("is").setExecutor(new IslandCommand(this));
        getCommand("is").setTabCompleter(new IslandTabCompleter(this));
        getCommand("panel").setExecutor(new PanelCommand(this));
        getCommand("oa").setExecutor(new AdminCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new IslandVisualListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OneBlockPlaceholders(this).register();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                databaseManager.saveData();
                getLogger().info("Automatyczny zapis danych...");
            }
        }.runTaskTimer(this, 6000L, 6000L);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.saveData();
        }
    }

    private void startMoneyGeneratorTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Island island : islandManager.getIslands().values()) {
                    if (island.getGenerator().getSelectedGeneratorLevel() == 7) {
                        double moneyToAdd = island.getGenerator().getMoneyPerSecond();
                        economyManager.addPlayerMoney(island.getOwnerId(), moneyToAdd);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    private void startScrafRemovalTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Island island : islandManager.getIslands().values()) {
                    island.getGenerator().checkAndRemoveScrafs();
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    public void showNotEnoughMoneyIndicator(Player player, int slot, ItemStack originalItem, Runnable resetCallback) {
        ItemStack noMoneyItem = new ItemStack(Material.PAPER);
        ItemMeta meta = noMoneyItem.getItemMeta();
        meta.setDisplayName("§cNie stać Cię na to!");
        meta.setCustomModelData(9997);
        noMoneyItem.setItemMeta(meta);

        if (player.getOpenInventory() != null) {
            player.getOpenInventory().setItem(slot, noMoneyItem);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getOpenInventory() != null && !player.getOpenInventory().getCursor().getType().equals(Material.AIR)) {
                        player.getOpenInventory().setCursor(new ItemStack(Material.AIR));
                    }
                    if (player.getOpenInventory() != null) {
                        player.getOpenInventory().setItem(slot, originalItem);
                    }
                    if (resetCallback != null) {
                        resetCallback.run();
                    }
                }
            }.runTaskLater(this, 60L);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
}