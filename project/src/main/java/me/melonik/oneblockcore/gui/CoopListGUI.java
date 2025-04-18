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

public class CoopListGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53};

    public CoopListGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8Lista coopów");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        // Ramka GUI
        for (int slot : BORDER_SLOTS) {

            ItemStack backButton = createItem(Material.PAPER, "§cPowrót!",
                    "§7Kliknij, aby wrócić do panelu wyspy"
            );

            ItemMeta meta = backButton.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(9996);
                backButton.setItemMeta(meta);
            }

            inventory.setItem(49, backButton);
        }

        int slot = 9;
        for (UUID coopId : island.getCoopPlayers()) {
            OfflinePlayer coop = Bukkit.getOfflinePlayer(coopId);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(coop);

            String status = coop.isOnline() ? "§aONLINE" : "§cOFFLINE";
            meta.setDisplayName("§3" + coop.getName() + " §8[" + status + "§8]");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Ten gracz posiada tymczasowe");
            lore.add("§7uprawnienia na twojej wyspie");
            lore.add("");
            lore.add("§cKliknij aby usunąć coopa");

            meta.setLore(lore);
            head.setItemMeta(meta);

            inventory.setItem(slot++, head);
        }

        // Przycisk edycji uprawnień
        ItemStack permissionsButton = createItem(Material.PAPER, "§eEdytuj uprawnienia coopów",
                "§7Kliknij, aby edytować uprawnienia",
                "§7dla wszystkich tymczasowych coopów"
        );

        ItemMeta meta = permissionsButton.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(9995);
            permissionsButton.setItemMeta(meta);
        }

        inventory.setItem(53, permissionsButton);
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

        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) return;

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§cTylko właściciel wyspy może zarządzać coopami!");
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (event.getSlot() == 53) {
            // Otwórz GUI edycji uprawnień coopów
            new CoopPermissionsGUI(plugin, player).open();
            return;
        }

        int slot = event.getSlot();
        if (slot == 49) {
            new IslandPanelGUI(plugin, player).open();
            return;
        }

        if (clicked.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            UUID coopId = meta.getOwningPlayer().getUniqueId();
            plugin.getIslandManager().removeCoop(island.getIslandId(), coopId);
            player.sendMessage("§cUsunięto coopa " + meta.getOwningPlayer().getName());

            Player coopPlayer = Bukkit.getPlayer(coopId);
            if (coopPlayer != null) {
                coopPlayer.sendMessage("§cTwoje tymczasowe uprawnienia na wyspie " + player.getName() + " zostały usunięte!");
            }

            initializeItems();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }
}