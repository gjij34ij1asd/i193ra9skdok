package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class MemberPermissionsGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final UUID targetId;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};

    public MemberPermissionsGUI(Main plugin, Player player, UUID targetId) {
        this.plugin = plugin;
        this.player = player;
        this.targetId = targetId;
        this.island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        this.inventory = Bukkit.createInventory(null, 54, "§8Uprawnienia członka");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack backButton = createItem(Material.PAPER, "§cPowrót!",
                "§7Kliknij, aby wrócić do panelu wyspy"
        );

        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setCustomModelData(9996);
            backButton.setItemMeta(backMeta);
        }

        inventory.setItem(49, backButton);
        for (int slot : BORDER_SLOTS) {
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        String playerName = target.getName();

        // Zarządzanie członkami
        addPermissionItem(10, Material.PAPER, "Dodawanie członków", "ADD_MEMBER", playerName);
        addPermissionItem(11, Material.PAPER, "Wyrzucanie członków", "KICK_MEMBER", playerName);

        // Podstawowe uprawnienia
        addPermissionItem(12, Material.GRAVEL, "Niszczenie bloków", "BREAK", playerName);
        addPermissionItem(13, Material.OAK_PLANKS, "Stawianie bloków", "BUILD", playerName);
        addPermissionItem(14, Material.BEACON, "Niszczenie magicznych latarni", "BEACON_BREAK", playerName);
        addPermissionItem(15, Material.CHEST, "Otwieranie skrzynek", "CHEST", playerName);
        addPermissionItem(16, Material.FURNACE, "Otwieranie pieców", "FURNACE", playerName);

        // Walka
        addPermissionItem(19, Material.IRON_SWORD, "Atakowanie potworów", "ATTACK_MOBS", playerName);
        addPermissionItem(20, Material.PORKCHOP, "Atakowanie zwierząt", "ATTACK_ANIMALS", playerName);

        // Interakcje
        addPermissionItem(21, Material.OAK_DOOR, "Interakcja z otwieraniem", "DOORS", playerName);
        addPermissionItem(22, Material.CLOCK, "Kupowanie ulepszeń", "UPGRADES", playerName);
        addPermissionItem(23, Material.STONE_PRESSURE_PLATE, "Używanie przycisków i płytek", "REDSTONE", playerName);

        // Spawnery
        addPermissionItem(24, Material.SPAWNER, "Stawianie/niszczenie spawnerów", "SPAWNER_MANAGE", playerName);
        addPermissionItem(25, Material.SPAWNER, "Zarządzanie spawnerami", "SPAWNER_SETTINGS", playerName);
    }

    private void addPermissionItem(int slot, Material material, String name, String permission, String playerName) {
        boolean enabled = island.hasPermission(targetId, permission);
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
        if (material == Material.SPAWNER) {
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot == 49) {
            new MemberListGUI(plugin, player).open();
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
            boolean currentState = island.hasPermission(targetId, permission);
            island.setPermission(targetId, permission, !currentState);
            plugin.getIslandManager().saveIslands();
            initializeItems();
        }
    }

    private String getPermissionFromSlot(int slot) {
        switch (slot) {
            case 10: return "ADD_MEMBER";
            case 11: return "KICK_MEMBER";
            case 12: return "BREAK";
            case 13: return "BUILD";
            case 14: return "BEACON_BREAK";
            case 15: return "CHEST";
            case 16: return "FURNACE";
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