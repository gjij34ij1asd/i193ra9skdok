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

public class MemberListGUI implements Listener {
    private final Main plugin;
    private final Player player;
    private final Inventory inventory;
    private final Island island;
    private static final int[] BORDER_SLOTS = {0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53};

    public MemberListGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        this.inventory = Bukkit.createInventory(null, 54, "§8Lista członków wyspy");
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

        // Dodaj głowę właściciela
        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerId());
        ItemStack ownerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta ownerMeta = (SkullMeta) ownerHead.getItemMeta();
        ownerMeta.setOwningPlayer(owner);

        String ownerStatus = owner.isOnline() ? "§aONLINE" : "§cOFFLINE";
        ownerMeta.setDisplayName("§3" + owner.getName() + " §8[" + ownerStatus + "§8]");

        List<String> ownerLore = new ArrayList<>();
        ownerLore.add("§7Wszystkie permisje dotyczące wyspy");
        ownerLore.add("§7są do Jego dyspozycji!");
        ownerLore.add("");
        ownerLore.add("§bLista uprawnień:");
        ownerLore.add("  §8• §7Jako właściciel wyspy posiada wszystkie");
        ownerLore.add("  §8• §7uprawnienia na wyspie");
        ownerLore.add("");
        ownerLore.add("§cNie możesz edytować uprawnień właścicielowi!");

        ownerMeta.setLore(ownerLore);
        ownerHead.setItemMeta(ownerMeta);
        inventory.setItem(10, ownerHead);

        // Członkowie wyspy
        int slot = 11;
        for (UUID memberId : island.getMembers()) {
            if (memberId.equals(island.getOwnerId())) continue;

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberMeta = (SkullMeta) head.getItemMeta();
            memberMeta.setOwningPlayer(member);

            String status = member.isOnline() ? "§aONLINE" : "§cOFFLINE";
            memberMeta.setDisplayName("§3" + member.getName() + " §8[" + status + "§8]");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§bLista uprawnień:");

            // Zarządzanie członkami
            lore.add(getPermissionString("Dodawanie członków", memberId, "ADD_MEMBER"));
            lore.add(getPermissionString("Wyrzucanie członków", memberId, "KICK_MEMBER"));

            // Podstawowe uprawnienia
            lore.add(getPermissionString("Niszczenie bloków", memberId, "BREAK"));
            lore.add(getPermissionString("Stawianie bloków", memberId, "BUILD"));
            lore.add(getPermissionString("Niszczenie magicznych latarni", memberId, "BEACON_BREAK"));
            lore.add(getPermissionString("Otwieranie skrzynek", memberId, "CHEST"));
            lore.add(getPermissionString("Otwieranie pieców", memberId, "FURNACE"));

            // Walka
            lore.add(getPermissionString("Atakowanie potworów", memberId, "ATTACK_MOBS"));
            lore.add(getPermissionString("Atakowanie zwierząt", memberId, "ATTACK_ANIMALS"));

            // Interakcje
            lore.add(getPermissionString("Interakcja z otwieraniem", memberId, "DOORS"));
            lore.add(getPermissionString("Kupowanie ulepszeń", memberId, "UPGRADES"));
            lore.add(getPermissionString("Używanie przycisków i płytek", memberId, "REDSTONE"));

            // Spawnery
            lore.add(getPermissionString("Stawianie/niszczenie spawnerów", memberId, "SPAWNER_MANAGE"));
            lore.add(getPermissionString("Zarządzanie spawnerami", memberId, "SPAWNER_SETTINGS"));

            lore.add("");
            lore.add("§eKliknij aby edytować uprawnienia");

            memberMeta.setLore(lore);
            head.setItemMeta(memberMeta);
            inventory.setItem(slot++, head);
        }
    }

    private String getPermissionString(String name, UUID playerId, String permission) {
        boolean hasPermission = island.hasPermission(playerId, permission);
        return String.format("  §8• §7%s §8[%s§8]", name, hasPermission ? "§a✔" : "§c❌");
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore.length > 0) {
            itemMeta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot == 49) {
            new IslandPanelGUI(plugin, player).open();
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§cTylko właściciel wyspy może zarządzać uprawnieniami!");
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        SkullMeta clickedMeta = (SkullMeta) clicked.getItemMeta();
        if (clickedMeta == null || clickedMeta.getOwningPlayer() == null) return;

        UUID targetId = clickedMeta.getOwningPlayer().getUniqueId();

        if (targetId.equals(island.getOwnerId())) {
            player.sendMessage("§cNie możesz edytować uprawnień właściciela!");
            return;
        }

        new MemberPermissionsGUI(plugin, player, targetId).open();
    }

    public void open() {
        player.openInventory(inventory);
    }
}
