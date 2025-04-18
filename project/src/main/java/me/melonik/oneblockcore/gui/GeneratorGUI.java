package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GeneratorGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26};
    private int noMoneySlot = -1;
    private boolean isClosing = false;

    public GeneratorGUI(Main plugin, Player player, Island island) {
        this.plugin = plugin;
        this.player = player;
        this.island = island;
        this.inventory = Bukkit.createInventory(null, 27, "§8Generator Skrafów");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        // Czarne szkło jako ramka
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§r");
        for (int i : BORDER_SLOTS) {
            inventory.setItem(i, border);
        }

        // Napędzacz (papier w środku)
        ItemStack napedzacz = createItem(Material.PAPER, "§aNapędzacz",
                "§7Aby napędzić generator, najedź kursorem",
                "§7ze skrafami na ten przedmiot.",
                "§7Zdobędziesz je na strefie X.",
                "",
                "§7Generator startuje z §61$ §7na sekundę",
                "§7Produkcja zależy od ilości §6Skrafów§7:",
                "§8• §7Każdy skraf dodaje §60.1$ §7na sekundę",
                "§8• §7Skrafy znikają co sekundę",
                "",
                String.format("§7Aktualna produkcja: §6%.1f$ §7na sekundę", island.getGenerator().getMoneyPerSecond()),
                String.format("§7Aktywne skrafy: §6%d", island.getGenerator().getScrafCount())
        );
        inventory.setItem(13, napedzacz);

        // Wypełnij pozostałe sloty czarnym szkłem
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
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

        // Pozwól na klikanie w dolny ekwipunek
        if (event.getRawSlot() >= inventory.getSize()) {
            return;
        }

        // Zablokuj wyciąganie przedmiotów z GUI
        event.setCancelled(true);

        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() == Material.NETHERITE_SCRAP) {
            if (cursor.hasItemMeta() && cursor.getItemMeta().getDisplayName().equals("§6Skrafa")) {
                if (event.getRawSlot() == 13 ||
                        (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER)) {
                    island.getGenerator().addScraf(cursor.getAmount());
                    event.getCursor().setAmount(0);
                    initializeItems();
                    plugin.getIslandManager().saveIslands();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Zablokuj przeciąganie tylko w górnej części inventory
            for (int slot : event.getRawSlots()) {
                if (slot < inventory.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
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