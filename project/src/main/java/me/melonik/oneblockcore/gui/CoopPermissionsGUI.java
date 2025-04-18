package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CoopPermissionsGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};

    public CoopPermissionsGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        this.inventory = Bukkit.createInventory(null, 54, "§8Uprawnienia coopów");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack backButton = createItem(Material.PAPER, "§cPowrót!",
                "§7Kliknij, aby wrócić do panelu wyspy"
        );

        ItemMeta meta = backButton.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(9996);
            backButton.setItemMeta(meta);
        }

        inventory.setItem(49, backButton);
        for (int slot : BORDER_SLOTS) {
        }

        // Podstawowe uprawnienia
        addPermissionItem(10, Material.GRAVEL, "Niszczenie bloków", "BREAK");
        addPermissionItem(11, Material.OAK_PLANKS, "Stawianie bloków", "BUILD");
        addPermissionItem(12, Material.BEACON, "Niszczenie magicznych latarni", "BEACON_BREAK");
        addPermissionItem(13, Material.CHEST, "Otwieranie skrzynek", "CHEST");
        addPermissionItem(14, Material.FURNACE, "Otwieranie pieców", "FURNACE");

        // Walka
        addPermissionItem(19, Material.IRON_SWORD, "Atakowanie potworów", "ATTACK_MOBS");
        addPermissionItem(20, Material.PORKCHOP, "Atakowanie zwierząt", "ATTACK_ANIMALS");

        // Interakcje
        addPermissionItem(21, Material.OAK_DOOR, "Interakcja z otwieraniem", "DOORS");
        addPermissionItem(22, Material.CLOCK, "Kupowanie ulepszeń", "UPGRADES");
        addPermissionItem(23, Material.STONE_PRESSURE_PLATE, "Używanie przycisków i płytek", "REDSTONE");

        // Spawnery
        addPermissionItem(24, Material.SPAWNER, "Stawianie/niszczenie spawnerów", "SPAWNER_MANAGE");
        addPermissionItem(25, Material.SPAWNER, "Zarządzanie spawnerami", "SPAWNER_SETTINGS");
    }

    private void addPermissionItem(int slot, Material material, String name, String permission) {
        boolean enabled = island.getCoopPermission(permission);
        ItemStack item = createItem(material,
                (enabled ? "§a" : "§c") + name,
                "§7Status: " + (enabled ? "§aWłączone" : "§cWyłączone"),
                "",
                "§7Kliknij aby " + (enabled ? "§cwyłączyć" : "§awłączyć")
        );
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

        int slot = event.getSlot();
        if (slot == 49) {
            new CoopListGUI(plugin, player).open();
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§cTylko właściciel wyspy może zarządzać uprawnieniami!");
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        String permission = getPermissionFromSlot(event.getSlot());
        if (permission != null) {
            boolean currentState = island.getCoopPermission(permission);
            island.setCoopPermission(permission, !currentState);
            plugin.getIslandManager().saveIslands();
            initializeItems();
        }
    }

    private String getPermissionFromSlot(int slot) {
        switch (slot) {
            case 10: return "BREAK";
            case 11: return "BUILD";
            case 12: return "BEACON_BREAK";
            case 13: return "CHEST";
            case 14: return "FURNACE";
            case 19: return "ATTACK_MOBS";
            case 20: return "ATTACK_ANIMALS";
            case 21: return "DOORS";
            case 22: return "UPGRADES";
            case 23: return "REDSTONE";
            case 24: return "SPAWNER_MANAGE";
            case 25: return "SPAWNER_SETTINGS";
            default: return null;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }
}