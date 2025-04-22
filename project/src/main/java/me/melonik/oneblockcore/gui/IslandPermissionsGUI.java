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
import java.util.UUID;

public class IslandPermissionsGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final UUID targetUUID;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};

    public IslandPermissionsGUI(Main plugin, Player player, UUID targetUUID) {
        this.plugin = plugin;
        this.player = player;
        this.targetUUID = targetUUID;
        this.inventory = Bukkit.createInventory(null, 54, "§8Uprawnienia członka");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        for (int slot : BORDER_SLOTS) {

        }

        addPermissionItem(10, Material.GRASS_BLOCK, "Stawianie bloków", island.hasPermission(targetUUID, "BUILD"));
        addPermissionItem(11, Material.DIAMOND_PICKAXE, "Niszczenie bloków", island.hasPermission(targetUUID, "BREAK"));
        addPermissionItem(12, Material.CHEST, "Otwieranie skrzynek", island.hasPermission(targetUUID, "CHEST"));
        addPermissionItem(13, Material.FURNACE, "Używanie pieców", island.hasPermission(targetUUID, "FURNACE"));
        addPermissionItem(14, Material.OAK_DOOR, "Używanie drzwi", island.hasPermission(targetUUID, "DOORS"));
        addPermissionItem(15, Material.IRON_SWORD, "PvP", island.hasPermission(targetUUID, "PVP"));
        addPermissionItem(16, Material.SPAWNER, "Używanie spawnerów", island.hasPermission(targetUUID, "SPAWNER"));

        addPermissionItem(19, Material.HOPPER, "Używanie lejków", island.hasPermission(targetUUID, "HOPPER"));
        addPermissionItem(20, Material.REDSTONE, "Używanie redstone", island.hasPermission(targetUUID, "REDSTONE"));
        addPermissionItem(21, Material.DIAMOND_HOE, "Używanie narzędzi", island.hasPermission(targetUUID, "TOOLS"));
        addPermissionItem(22, Material.BUCKET, "Używanie wiader", island.hasPermission(targetUUID, "BUCKET"));
        addPermissionItem(23, Material.ANVIL, "Używanie kowadeł", island.hasPermission(targetUUID, "ANVIL"));
        addPermissionItem(24, Material.BREWING_STAND, "Używanie alchemii", island.hasPermission(targetUUID, "BREWING"));
        addPermissionItem(25, Material.BEACON, "Używanie beaconów", island.hasPermission(targetUUID, "BEACON"));
    }

    private void addPermissionItem(int slot, Material material, String name, boolean enabled) {
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

        Player player = (Player) event.getWhoClicked();
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§cTylko właściciel wyspy może zarządzać uprawnieniami!");
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        String permission = getPermissionFromSlot(event.getSlot());
        if (permission != null) {
            boolean currentState = island.hasPermission(targetUUID, permission);
            island.setPermission(targetUUID, permission, !currentState);
            plugin.getIslandManager().saveIslands();
            initializeItems();
        }
    }

    private String getPermissionFromSlot(int slot) {
        switch (slot) {
            case 10: return "BUILD";
            case 11: return "BREAK";
            case 12: return "CHEST";
            case 13: return "FURNACE";
            case 14: return "DOORS";
            case 15: return "PVP";
            case 16: return "SPAWNER";
            case 19: return "HOPPER";
            case 20: return "REDSTONE";
            case 21: return "TOOLS";
            case 22: return "BUCKET";
            case 23: return "ANVIL";
            case 24: return "BREWING";
            case 25: return "BEACON";
            default: return null;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }
}