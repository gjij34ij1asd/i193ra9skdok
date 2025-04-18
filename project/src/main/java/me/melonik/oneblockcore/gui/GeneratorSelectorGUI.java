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
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26};

    public GeneratorSelectorGUI(Main plugin, Player player, Island island) {
        this.plugin = plugin;
        this.player = player;
        this.island = island;
        this.inventory = Bukkit.createInventory(null, 27, "§8Wybór Generatora");
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
        createGeneratorItem(11, Material.GRASS_BLOCK, "§aGenerator Ziemi", 1);

        // Generator poziomu 2 - Wood
        createGeneratorItem(12, Material.OAK_LOG, "§6Generator Drewna", 2);

        // Generator poziomu 3 - Stone
        createGeneratorItem(13, Material.STONE, "§7Generator Kamienia", 3);

        // Generator poziomu 4 - Ores
        createGeneratorItem(14, Material.DIAMOND_ORE, "§bGenerator Rud", 4);

        // Generator poziomu 5 - Nether
        createGeneratorItem(15, Material.NETHERRACK, "§cGenerator Netheru", 5);

        // Generator poziomu 6 - End
        createGeneratorItem(16, Material.END_STONE, "§5Generator Endu", 6);

        // Generator poziomu 7 - Ultimate
        if (island.getMaxLevel() >= 7) {
            ItemStack item = createItem(Material.BLAST_FURNACE, "§4Generator Ultimate",
                    "§7Poziom generatora: §f7",
                    "",
                    "§7Kliknij aby ustawić ten generator"
            );
            inventory.setItem(22, item);
        } else {
            ItemStack item = createItem(Material.BARRIER, "§8§l???",
                    "§cNieodblokowane",
                    "§7Wymagany poziom: §f7"
            );
            inventory.setItem(22, item);
        }
    }

    private void createGeneratorItem(int slot, Material material, String name, int level) {
        if (island.getMaxLevel() >= level) {
            ItemStack item = createItem(material, name,
                    "§7Poziom generatora: §f" + level,
                    "",
                    "§7Kliknij aby ustawić ten generator"
            );
            inventory.setItem(slot, item);
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
            player.sendMessage("§cTylko właściciel wyspy może zmieniać generator!");
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BARRIER) return;

        int selectedLevel = getLevelFromMaterial(clickedItem.getType());
        if (selectedLevel > 0 && selectedLevel <= island.getMaxLevel()) {
            // Wyczyść skrafy przy zmianie generatora
            island.getGenerator().clearScrafs();
            island.getGenerator().setSelectedGeneratorLevel(selectedLevel);
            island.getGenerator().setProgress(0); // Reset postępu przy zmianie generatora
            Location center = island.getCenter();
            center.getBlock().setType(clickedItem.getType());
            player.sendMessage("§aZmieniono typ generatora!");
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