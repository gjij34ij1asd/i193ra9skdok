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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IslandPanelGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private static final int[] BORDER_SLOTS = {0,8};

    public IslandPanelGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 9, "§8Panel Wyspy");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;
        for (int slot : BORDER_SLOTS) {
        }

        ItemStack generator = createItem(Material.BLAST_FURNACE, "§bZarządzanie generatorem",
                "§7Kliknij aby zmienić typ generatora"
        );
        inventory.setItem(1, generator);

        ItemStack settings = createItem(Material.REDSTONE_BLOCK, "§bUstawienia wyspy",
                "§7Kliknij aby otworzyć ustawienia!"
        );
        inventory.setItem(2, settings);

        ItemStack permissions = createItem(Material.PLAYER_HEAD, "§bEdytuj permisje członków",
                "§7Kliknij, aby zarządzać uprawnieniami",
                "§7członków twojej wyspy"
        );
        inventory.setItem(3, permissions);

        ItemStack upgrades = createItem(Material.EXPERIENCE_BOTTLE, "§bUlepszenia wyspy",
                "§7Kliknij, aby otworzyć menu ulepszeń"
        );
        inventory.setItem(5, upgrades);

        ItemStack banned = createItem(Material.BARRIER, "§bZbanowani gracze",
                "§7Kliknij aby zobaczyć listę",
                "§7zbanowanych graczy"
        );
        inventory.setItem(6, banned);

        ItemStack coops = createItem(Material.PAPER, "§bZarządzanie coopami",
                "§7Kliknij, aby zarządzać tymczasowymi",
                "§7uprawnieniami na wyspie"
        );
        inventory.setItem(7, coops);
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
            player.sendMessage("§cTylko właściciel wyspy może korzystać z panelu!");
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (event.getSlot()) {
            case 1:
                new GeneratorSelectorGUI(plugin, player, island).open();
                break;
            case 2:
                new IslandSettingsGUI(plugin, player).open();
                break;
            case 3:
                new MemberListGUI(plugin, player).open();
                break;
            case 5:
                new IslandUpgradesGUI(plugin, player).open();
                break;
            case 6:
                openBannedPlayersGUI();
                break;
            case 7:
                new CoopListGUI(plugin, player).open();
                break;
        }
    }

    private void openBannedPlayersGUI() {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        Inventory bannedInventory = Bukkit.createInventory(null, 54, "§8Zbanowani gracze");


        int slot = 10;
        for (UUID bannedId : island.getBanned()) {
            if (slot > 15) break;
            OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedId);
            if (bannedPlayer.getName() != null) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(bannedPlayer);
                meta.setDisplayName("§c" + bannedPlayer.getName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§bTen gracz jest zbanowany na twojej wyspie!");
                lore.add("§bKliknij aby go odbanować gracza!");
                meta.setLore(lore);
                head.setItemMeta(meta);
                bannedInventory.setItem(slot++, head);
            }
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!event.getInventory().equals(bannedInventory)) return;
                event.setCancelled(true);

                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        UUID bannedId = meta.getOwningPlayer().getUniqueId();
                        plugin.getIslandManager().unbanPlayer(island.getIslandId(), bannedId);
                        player.sendMessage("§aOdbanowano gracza " + meta.getOwningPlayer().getName());
                        player.closeInventory();
                        openBannedPlayersGUI();
                    }
                }
            }
        }, plugin);

        player.openInventory(bannedInventory);
    }

    public void open() {
        player.openInventory(inventory);
    }
}