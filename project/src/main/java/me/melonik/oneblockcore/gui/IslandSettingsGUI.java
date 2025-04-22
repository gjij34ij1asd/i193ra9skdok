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

public class IslandSettingsGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,9};

    public IslandSettingsGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        this.inventory = Bukkit.createInventory(null, 27, "§8Ustawienia wyspy");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeItems();
    }

    private void initializeItems() {
        for (int slot : BORDER_SLOTS) {
        }

        ItemStack timeItem = createItem(Material.CLOCK, "§3Zmiana pory dnia",
                "§7Aktualnie: " + (island.isAlwaysDay() ? "§aDzień" : "§9Noc"),
                "",
                "§7Kliknij aby zmienić porę dnia"
        );
        inventory.setItem(11, timeItem);

        ItemStack animalAttackItem = createItem(Material.BEEF, "§3Atakowanie zwierząt",
                "§7Status: " + (island.isAnimalDamage() ? "§aWłączone" : "§cWyłączone"),
                "",
                "§7Kliknij aby " + (island.isAnimalDamage() ? "§cwyłączyć" : "§awłączyć")
        );
        inventory.setItem(12, animalAttackItem);

        ItemStack mobAttackItem = createItem(Material.ZOMBIE_HEAD, "§3Atakowanie mobów",
                "§7Status: " + (island.isMobDamage() ? "§aWłączone" : "§cWyłączone"),
                "",
                "§7Kliknij aby " + (island.isMobDamage() ? "§cwyłączyć" : "§awłączyć")
        );
        inventory.setItem(13, mobAttackItem);

        ItemStack visitItem = createItem(Material.OAK_DOOR, "§3Odwiedzanie wyspy",
                "§7Status: " + (island.isVisitable() ? "§aWłączone" : "§cWyłączone"),
                "",
                "§7Kliknij aby " + (island.isVisitable() ? "§cwyłączyć" : "§awłączyć")
        );
        inventory.setItem(14, visitItem);

        ItemStack pickupItem = createItem(Material.HOPPER, "§3Podnoszenie przedmiotów",
                "§7Status: " + (island.isPickupItems() ? "§aWłączone" : "§cWyłączone"),
                "",
                "§7Kliknij aby " + (island.isPickupItems() ? "§cwyłączyć" : "§awłączyć")
        );
        inventory.setItem(15, pickupItem);

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

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendTitle("§4§lBłąd!", "§cTylko właściciel wyspy może zmieniać ustawienia!", 10, 40, 20);
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        switch (event.getSlot()) {
            case 11:
                island.setAlwaysDay(!island.isAlwaysDay());
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (island.isOnIsland(p)) {
                        p.setPlayerTime(island.isAlwaysDay() ? 6000 : 18000, false);
                    }
                }
                break;
            case 12:
                island.setAnimalDamage(!island.isAnimalDamage());
                break;
            case 13:
                island.setMobDamage(!island.isMobDamage());
                break;
            case 14:
                island.setVisitable(!island.isVisitable());
                break;
            case 15:
                island.setPickupItems(!island.isPickupItems());
                break;
            case 22:
                new IslandPanelGUI(plugin, player).open();
                return;
        }

        plugin.getIslandManager().saveIslands();
        initializeItems();
    }

    public void open() {
        player.openInventory(inventory);
    }
}