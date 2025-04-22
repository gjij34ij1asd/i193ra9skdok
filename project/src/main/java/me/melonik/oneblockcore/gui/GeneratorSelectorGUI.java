package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GeneratorSelectorGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,8};

    public GeneratorSelectorGUI(Main plugin, Player player, Island island) {
        this.plugin = plugin;
        this.player = player;
        this.island = island;
        this.inventory = Bukkit.createInventory(null, 9, "§8Zarządzanie Generatorem");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        // Ramka GUI
        ItemStack borderFrame = createItem(Material.BLACK_STAINED_GLASS_PANE, "§r");
        for (int slot : BORDER_SLOTS) {
            inventory.setItem(slot, borderFrame);
        }

        // Generator poziomu 1 - Dirt
        createGeneratorItem(1, Material.GRASS_BLOCK, "§aGenerator Ziemi", 1);

        // Generator poziomu 2 - Wood
        createGeneratorItem(2, Material.OAK_LOG, "§6Generator Drewna", 2);

        // Generator poziomu 3 - Stone
        createGeneratorItem(3, Material.STONE, "§7Generator Kamienia", 3);

        // Generator poziomu 4 - Ores
        createGeneratorItem(4, Material.DIAMOND_ORE, "§bGenerator Rud", 4);

        // Generator poziomu 5 - Nether
        createGeneratorItem(5, Material.NETHERRACK, "§cGenerator Piekła", 5);

        // Generator poziomu 6 - End
        createGeneratorItem(6, Material.END_STONE, "§5Generator Kresu", 6);

        // Generator poziomu 7 - Ultimate
        if (island.getMaxLevel() >= 7) {
            ItemStack item = createItem(Material.BLAST_FURNACE, "§2Generator Pradawny",
                    "§7Maksymalny poziom generatora! §f(7)",
                    "",
                    "§7Kliknij aby ustawić ten generator"
            );
            inventory.setItem(7, item);
        } else {
            ItemStack item = createItem(Material.BARRIER, "§8§l???",
                    "§cNieodblokowane",
                    "§7Wymagany poziom: §f7"
            );
            inventory.setItem(7, item);
        }
    }

    private void createGeneratorItem(int slot, Material material, String name, int level) {
        if (island.getMaxLevel() >= level) {
            if (level == island.getGenerator().getSelectedGeneratorLevel()) {
                ItemStack item = createItem(material, name,
                        "§7Poziom generatora: §f" + level,
                        "",
                        "§cTen generator jest aktualnie wybrany!");
                inventory.setItem(slot, item);
            } else {
                ItemStack item = createItem(material, name,
                        "§7Poziom generatora: §f" + level,
                        "",
                        "§7Kliknij aby ustawić ten generator");
                inventory.setItem(slot, item);
            }
        } else {
            ItemStack item = createItem(Material.BARRIER, "§8§l???",
                    "§cNieodblokowane",
                    "§7Wymagany poziom: §f" + level
            );
            inventory.setItem(slot, item);
        }
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

        Player player = (Player) event.getWhoClicked();
        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendTitle("§4§lBłąd!", "§cTylko właściciel może to zrobić!", 10, 40, 20);
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BARRIER) return;

        int selectedLevel = getLevelFromMaterial(clickedItem.getType());
        if (selectedLevel > 0 && selectedLevel <= island.getMaxLevel()) {
            if (selectedLevel == island.getGenerator().getSelectedGeneratorLevel()) {
                player.sendTitle("§4§lBłąd!", "§cTen poziom generator jest już wybrany!", 10, 40, 20);
                player.closeInventory();
                return;
            }

            island.getGenerator().clearScrafs();
            island.getGenerator().setSelectedGeneratorLevel(selectedLevel);

            Location center = island.getCenter();
            center.getBlock().setType(clickedItem.getType());
            player.sendTitle("§2§lSuckes!", "§aPomyślnie zmieniono poziom generatora!", 10, 40, 20);
            plugin.getIslandManager().saveIslands();
            player.closeInventory();
        }
    }

    private int getLevelFromMaterial(Material material) {
        switch (material) {
            case GRASS_BLOCK: return 1;
            case OAK_LOG: return 2;
            case STONE: return 3;
            case DIAMOND_ORE: return 4;
            case NETHERRACK: return 5;
            case END_STONE: return 6;
            case BLAST_FURNACE: return 7;
            default: return 0;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }
}