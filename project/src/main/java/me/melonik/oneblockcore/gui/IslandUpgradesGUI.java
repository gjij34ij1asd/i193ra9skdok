package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import me.melonik.oneblockcore.models.IslandUpgrades;
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

import java.util.*;

public class IslandUpgradesGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9,17,18,26};
    private int currentNoMoneySlot = -1;
    private boolean isClosing = false;

    public IslandUpgradesGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, "§8Ulepszenia Wyspy");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        // Ramka GUI
        for (int slot : BORDER_SLOTS) {
        }

        // Border
        ItemStack borderItem = createItem(Material.IRON_BARS, "§6Border wyspy",
                "§7Obecny rozmiar: §f" + island.getBorderSize() + "x" + island.getBorderSize(),
                "§7Następny rozmiar: §f" + IslandUpgrades.getNextBorderSize(island.getBorderSize()) + "x" + IslandUpgrades.getNextBorderSize(island.getBorderSize()),
                "§7Koszt ulepszenia: §f$" + IslandUpgrades.getBorderUpgradeCost(island.getBorderSize()),
                "",
                IslandUpgrades.getBorderUpgradeCost(island.getBorderSize()) > 0 ? "§eKliknij, aby ulepszyć" : "§cOsiągnięto maksymalny poziom"
        );
        inventory.setItem(11, borderItem);

        // Spawnery
        ItemStack spawnerItem = createItem(Material.SPAWNER, "§6Limit spawnerów",
                "§7Obecny limit: §f" + island.getUpgrades().getSpawnerLimit(),
                "§7Następny limit: §f" + IslandUpgrades.getNextSpawnerLimit(island.getUpgrades().getSpawnerLimit()),
                "§7Koszt ulepszenia: §f$" + IslandUpgrades.getSpawnerUpgradeCost(island.getUpgrades().getSpawnerLimit()),
                "",
                IslandUpgrades.getSpawnerUpgradeCost(island.getUpgrades().getSpawnerLimit()) > 0 ? "§eKliknij, aby ulepszyć" : "§cOsiągnięto maksymalny poziom"
        );
        inventory.setItem(12, spawnerItem);

        // Lejki
        ItemStack hopperItem = createItem(Material.HOPPER, "§6Limit lejków",
                "§7Obecny limit: §f" + island.getUpgrades().getHopperLimit(),
                "§7Następny limit: §f" + IslandUpgrades.getNextHopperLimit(island.getUpgrades().getHopperLimit()),
                "§7Koszt ulepszenia: §f$" + IslandUpgrades.getHopperUpgradeCost(island.getUpgrades().getHopperLimit()),
                "",
                IslandUpgrades.getHopperUpgradeCost(island.getUpgrades().getHopperLimit()) > 0 ? "§eKliknij, aby ulepszyć" : "§cOsiągnięto maksymalny poziom"
        );
        inventory.setItem(13, hopperItem);

        // Członkowie
        ItemStack memberItem = createItem(Material.PLAYER_HEAD, "§6Limit członków",
                "§7Obecny limit: §f" + island.getUpgrades().getMemberLimit(),
                "§7Następny limit: §f" + IslandUpgrades.getNextMemberLimit(island.getUpgrades().getMemberLimit()),
                "§7Koszt ulepszenia: §f$" + IslandUpgrades.getMemberUpgradeCost(island.getUpgrades().getMemberLimit()),
                "",
                IslandUpgrades.getMemberUpgradeCost(island.getUpgrades().getMemberLimit()) > 0 ? "§eKliknij, aby ulepszyć" : "§cOsiągnięto maksymalny poziom"
        );
        inventory.setItem(14, memberItem);

        // Pistony
        ItemStack pistonItem = createItem(Material.PISTON, "§6Limit pistonów",
                "§7Obecny limit: §f" + island.getUpgrades().getPistonLimit(),
                "§7Następny limit: §f" + IslandUpgrades.getNextPistonLimit(island.getUpgrades().getPistonLimit()),
                "§7Koszt ulepszenia: §f$" + IslandUpgrades.getPistonUpgradeCost(island.getUpgrades().getPistonLimit()),
                "",
                IslandUpgrades.getPistonUpgradeCost(island.getUpgrades().getPistonLimit()) > 0 ? "§eKliknij, aby ulepszyć" : "§cOsiągnięto maksymalny poziom"
        );
        inventory.setItem(15, pistonItem);

        // Przycisk powrotu
        ItemStack backButton = createItem(Material.PAPER, "§cPowrót!",
                "§7Kliknij, aby wrócić do panelu wyspy"
        );

        ItemMeta meta = backButton.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(9996);
            backButton.setItemMeta(meta);
        }

        inventory.setItem(22, backButton);
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
            player.sendMessage("§cTylko właściciel wyspy może korzystać z ulepszeń!");
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        int slot = event.getSlot();

        // Jeśli kliknięto przycisk powrotu
        if (slot == 22) {
            new IslandPanelGUI(plugin, player).open();
            return;
        }

        // Sprawdź czy slot jest prawidłowy dla ulepszeń
        if (slot < 11 || slot > 15) return;

        // Jeśli już wyświetlamy wskaźnik braku pieniędzy, przenieś go do nowego slotu
        if (currentNoMoneySlot != -1) {
            // Przywróć oryginalny item w poprzednim slocie
            initializeItems();
        }

        double cost = getUpgradeCost(island, slot);
        if (cost <= 0) {
            player.sendMessage("§cOsiągnięto maksymalny poziom!");
            return;
        }

        if (plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()) < cost) {
            // Zapisz aktualny slot ze wskaźnikiem
            currentNoMoneySlot = slot;

            // Pokaż wskaźnik braku pieniędzy
            ItemStack noMoneyItem = new ItemStack(Material.PAPER);
            ItemMeta meta = noMoneyItem.getItemMeta();
            meta.setDisplayName("§cNie stać Cię na to!");
            meta.setLore(Arrays.asList("§7Potrzebujesz jeszcze: §c$" + (cost - plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()))));
            meta.setCustomModelData(9997);
            noMoneyItem.setItemMeta(meta);

            inventory.setItem(slot, noMoneyItem);
            return;
        }

        handleUpgrade(island, slot);
    }

    private double getUpgradeCost(Island island, int slot) {
        switch (slot) {
            case 11: return IslandUpgrades.getBorderUpgradeCost(island.getBorderSize());
            case 12: return IslandUpgrades.getSpawnerUpgradeCost(island.getUpgrades().getSpawnerLimit());
            case 13: return IslandUpgrades.getHopperUpgradeCost(island.getUpgrades().getHopperLimit());
            case 14: return IslandUpgrades.getMemberUpgradeCost(island.getUpgrades().getMemberLimit());
            case 15: return IslandUpgrades.getPistonUpgradeCost(island.getUpgrades().getPistonLimit());
            default: return -1;
        }
    }

    private void handleUpgrade(Island island, int slot) {
        double cost = getUpgradeCost(island, slot);
        if (cost <= 0) return;

        if (!plugin.getEconomyManager().removePlayerMoney(player.getUniqueId(), cost)) {
            return;
        }

        switch (slot) {
            case 11:
                island.setBorderSize(IslandUpgrades.getNextBorderSize(island.getBorderSize()));
                player.sendMessage("§aUlepszono rozmiar bordera!");
                break;
            case 12:
                island.getUpgrades().setSpawnerLimit(IslandUpgrades.getNextSpawnerLimit(island.getUpgrades().getSpawnerLimit()));
                player.sendMessage("§aUlepszono limit spawnerów!");
                break;
            case 13:
                island.getUpgrades().setHopperLimit(IslandUpgrades.getNextHopperLimit(island.getUpgrades().getHopperLimit()));
                player.sendMessage("§aUlepszono limit lejków!");
                break;
            case 14:
                island.getUpgrades().setMemberLimit(IslandUpgrades.getNextMemberLimit(island.getUpgrades().getMemberLimit()));
                player.sendMessage("§aUlepszono limit członków!");
                break;
            case 15:
                island.getUpgrades().setPistonLimit(IslandUpgrades.getNextPistonLimit(island.getUpgrades().getPistonLimit()));
                player.sendMessage("§aUlepszono limit pistonów!");
                break;
        }

        plugin.getIslandManager().saveIslands();
        initializeItems();
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
        currentNoMoneySlot = -1;
    }

    public void open() {
        player.openInventory(inventory);
    }
}