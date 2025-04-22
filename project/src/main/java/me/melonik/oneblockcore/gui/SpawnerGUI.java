package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.CustomSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SpawnerGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final CustomSpawner spawner;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
    private int noMoneySlot = -1;
    private boolean isClosing = false;

    public SpawnerGUI(Main plugin, Player player, CustomSpawner spawner) {
        this.plugin = plugin;
        this.player = player;
        this.spawner = spawner;
        this.inventory = Bukkit.createInventory(null, 54, "§8Ustawienia Spawnera");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack borderFrame = createItem(Material.BLACK_STAINED_GLASS_PANE, "§r");
        for (int slot : BORDER_SLOTS) {
            inventory.setItem(slot, borderFrame);
        }

        // Informacje o spawnerze
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Typ moba: " + (spawner.getEntityType() != null ? "§a" + spawner.getEntityType().name() : "§cNie ustawiono"));
        infoLore.add("§7Szybkość: §a" + spawner.getMobsPerMinute() + " mobów/minutę");
        infoLore.add("§7Status: " + (spawner.isEnabled() ? "§aWłączony" : "§cWyłączony"));

        ItemStack info = createItem(Material.SPAWNER, "§6Informacje o spawnerze", infoLore.toArray(new String[0]));
        inventory.setItem(4, info);

        // Moby - tylko jeden dostępny na raz
        if (spawner.getEntityType() == null) {
            addMobOption(19, EntityType.PIG, Material.PORKCHOP, "Świnka", 10000);
            addMobOption(20, EntityType.SHEEP, Material.WHITE_WOOL, "Owca", 30000);
            addMobOption(21, EntityType.COW, Material.BEEF, "Krowa", 50000);
            addMobOption(22, EntityType.SPIDER, Material.SPIDER_EYE, "Pająk", 100000);
            addMobOption(23, EntityType.SKELETON, Material.BONE, "Szkielet", 140000);
            addMobOption(24, EntityType.ZOMBIE, Material.ROTTEN_FLESH, "Zombie", 200000);
            addMobOption(25, EntityType.CREEPER, Material.GUNPOWDER, "Creeper", 500000);
        } else {
            // Pokaż tylko aktualnie wybrany mob
            int slot = getMobSlot(spawner.getEntityType());
            if (slot != -1) {
                addSelectedMobOption(slot, spawner.getEntityType());
                // Wypełnij pozostałe sloty szkłem
                for (int i = 19; i <= 25; i++) {
                    if (i != slot) {
                        inventory.setItem(i, borderFrame);
                    }
                }
            }
        }

        // Ulepszenia szybkości
        for (int i = 0; i <= 10; i++) {
            int slot = 37 + i;
            if (i <= spawner.getSpeedLevel()) {
                inventory.setItem(slot, createItem(Material.LIME_STAINED_GLASS_PANE,
                        "§aPoziom " + i,
                        "§7Szybkość: §a" + getSpeedForLevel(i) + " mobów/minutę",
                        "§aOdblokowane"));
            } else if (i == spawner.getSpeedLevel() + 1) {
                int cost = CustomSpawner.getSpeedUpgradeCost(i);
                inventory.setItem(slot, createItem(Material.RED_STAINED_GLASS_PANE,
                        "§cPoziom " + i,
                        "§7Szybkość: §a" + getSpeedForLevel(i) + " mobów/minutę",
                        "§7Koszt: §6$" + cost,
                        "",
                        "§eKliknij aby ulepszyć"));
            } else {
                inventory.setItem(slot, createItem(Material.BLACK_STAINED_GLASS_PANE,
                        "§8Poziom " + i,
                        "§7Wymagany poziom: §c" + (i - 1),
                        "§cNiedostępne"));
            }
        }

        // Jeśli jest aktywny slot "brak pieniędzy", pokaż go
        if (noMoneySlot != -1) {
            ItemStack noMoneyItem = new ItemStack(Material.PAPER);
            ItemMeta meta = noMoneyItem.getItemMeta();
            meta.setDisplayName("§cNie stać Cię na to!");
            meta.setLore(Arrays.asList(
                    "§7Potrzebujesz jeszcze: §c$" +
                            String.format("%.2f", (getUpgradeCost() - plugin.getEconomyManager().getPlayerMoney(player.getUniqueId())))
            ));
            noMoneyItem.setItemMeta(meta);
            inventory.setItem(noMoneySlot, noMoneyItem);
        }
    }

    private int getMobSlot(EntityType type) {
        switch (type) {
            case PIG: return 19;
            case SHEEP: return 20;
            case COW: return 21;
            case SPIDER: return 22;
            case SKELETON: return 23;
            case ZOMBIE: return 24;
            case CREEPER: return 25;
            default: return -1;
        }
    }

    private double getUpgradeCost() {
        if (noMoneySlot >= 37 && noMoneySlot <= 47) {
            return CustomSpawner.getSpeedUpgradeCost(noMoneySlot - 37);
        } else {
            return CustomSpawner.getMobUpgradeCost(getEntityTypeFromSlot(noMoneySlot));
        }
    }

    private EntityType getEntityTypeFromSlot(int slot) {
        switch (slot) {
            case 19: return EntityType.PIG;
            case 20: return EntityType.SHEEP;
            case 21: return EntityType.COW;
            case 22: return EntityType.SPIDER;
            case 23: return EntityType.SKELETON;
            case 24: return EntityType.ZOMBIE;
            case 25: return EntityType.CREEPER;
            default: return null;
        }
    }

    private void addSelectedMobOption(int slot, EntityType type) {
        Material material = getMaterialForEntity(type);
        String name = getNameForEntity(type);
        ItemStack item = createItem(material, "§a" + name,
                "§aAktualnie wybrany mob",
                "",
                "§7Kliknij aby zmienić typ moba");
        inventory.setItem(slot, item);
    }

    private Material getMaterialForEntity(EntityType type) {
        switch (type) {
            case PIG: return Material.PORKCHOP;
            case SHEEP: return Material.WHITE_WOOL;
            case COW: return Material.BEEF;
            case SPIDER: return Material.SPIDER_EYE;
            case SKELETON: return Material.BONE;
            case ZOMBIE: return Material.ROTTEN_FLESH;
            case CREEPER: return Material.GUNPOWDER;
            default: return Material.BARRIER;
        }
    }

    private String getNameForEntity(EntityType type) {
        switch (type) {
            case PIG: return "Świnka";
            case SHEEP: return "Owca";
            case COW: return "Krowa";
            case SPIDER: return "Pająk";
            case SKELETON: return "Szkielet";
            case ZOMBIE: return "Zombie";
            case CREEPER: return "Creeper";
            default: return "Nieznany";
        }
    }

    private int getSpeedForLevel(int level) {
        switch (level) {
            case 0: return 50;
            case 1: return 70;
            case 2: return 100;
            case 3: return 200;
            case 4: return 500;
            case 5: return 1000;
            case 6: return 2000;
            case 7: return 3000;
            case 8: return 4000;
            case 9: return 5000;
            case 10: return 10000;
            default: return 50;
        }
    }

    private void addMobOption(int slot, EntityType type, Material material, String name, int cost) {
        boolean isSelected = spawner.getEntityType() == type;
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isSelected) {
            lore.add("§aAktualnie wybrany");
        } else {
            lore.add("§7Koszt: §6$" + cost);
            lore.add("");
            lore.add("§eKliknij aby wybrać");
        }

        ItemStack item = createItem(material, "§b" + name, lore.toArray(new String[0]));
        inventory.setItem(slot, item);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        int slot = event.getSlot();

        // Jeśli kliknięto w slot z "brak pieniędzy", zignoruj
        if (slot == noMoneySlot) return;

        // Ulepszanie szybkości
        if (slot >= 37 && slot <= 47) {
            int level = slot - 37;
            if (level == spawner.getSpeedLevel() + 1) {
                int cost = CustomSpawner.getSpeedUpgradeCost(level);
                if (cost > 0) {
                    if (plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()) >= cost) {
                        plugin.getEconomyManager().removePlayerMoney(player.getUniqueId(), cost);
                        spawner.setSpeedLevel(level);
                        player.sendMessage("§aUlepszono szybkość spawnera!");
                        initializeItems();
                    } else {
                        // Usuń poprzedni slot "brak pieniędzy" jeśli istnieje
                        if (noMoneySlot != -1 && noMoneySlot != slot) {
                            inventory.setItem(noMoneySlot, getOriginalItem(noMoneySlot));
                        }
                        noMoneySlot = slot;
                        initializeItems();
                    }
                }
            }
            return;
        }

        // Wybór moba
        if (slot >= 19 && slot <= 25) {
            if (spawner.getEntityType() != null) {
                spawner.setEntityType(null);
                initializeItems();
                return;
            }

            EntityType selectedType = getEntityTypeFromSlot(slot);
            if (selectedType != null && spawner.getEntityType() != selectedType) {
                int cost = CustomSpawner.getMobUpgradeCost(selectedType);
                if (plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()) >= cost) {
                    plugin.getEconomyManager().removePlayerMoney(player.getUniqueId(), cost);
                    spawner.setEntityType(selectedType);
                    player.sendMessage("§aZmieniono typ moba!");
                    initializeItems();
                } else {
                    // Usuń poprzedni slot "brak pieniędzy" jeśli istnieje
                    if (noMoneySlot != -1 && noMoneySlot != slot) {
                        inventory.setItem(noMoneySlot, getOriginalItem(noMoneySlot));
                    }
                    noMoneySlot = slot;
                    initializeItems();
                }
            }
        }
    }

    private ItemStack getOriginalItem(int slot) {
        if (slot >= 37 && slot <= 47) {
            int level = slot - 37;
            if (level == spawner.getSpeedLevel() + 1) {
                int cost = CustomSpawner.getSpeedUpgradeCost(level);
                return createItem(Material.RED_STAINED_GLASS_PANE,
                        "§cPoziom " + level,
                        "§7Szybkość: §a" + getSpeedForLevel(level) + " mobów/minutę",
                        "§7Koszt: §6$" + cost,
                        "",
                        "§eKliknij aby ulepszyć");
            }
        } else if (slot >= 19 && slot <= 25) {
            EntityType type = getEntityTypeFromSlot(slot);
            if (type != null) {
                return createItem(getMaterialForEntity(type),
                        "§b" + getNameForEntity(type),
                        "",
                        "§7Koszt: §6$" + CustomSpawner.getMobUpgradeCost(type),
                        "",
                        "§eKliknij aby wybrać");
            }
        }
        return createItem(Material.BLACK_STAINED_GLASS_PANE, "§r");
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        isClosing = true;
        noMoneySlot = -1;
    }

    public void open() {
        player.openInventory(inventory);
    }
}