package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.gui.SpawnerGUI;
import me.melonik.oneblockcore.models.CustomSpawner;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerListener implements Listener {
    private final Main plugin;

    public SpawnerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());

        if (island == null) return;

        if (!island.getOwnerId().equals(player.getUniqueId()) &&
                !island.hasPermission(player.getUniqueId(), "SPAWNER_MANAGE")) {
            event.setCancelled(true);
            player.sendMessage("§cNie masz uprawnień do stawiania spawnerów!");
            return;
        }

        // Sprawdź limit spawnerów
        int currentSpawners = plugin.getSpawnerManager().getSpawners().size();
        if (currentSpawners >= island.getUpgrades().getSpawnerLimit()) {
            event.setCancelled(true);
            player.sendMessage("§cOsiągnięto maksymalny limit spawnerów na wyspie! §7(" + currentSpawners + "/" + island.getUpgrades().getSpawnerLimit() + ")");
            return;
        }

        plugin.getSpawnerManager().addSpawner(block.getLocation());
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());

        if (island == null) return;

        if (!island.getOwnerId().equals(player.getUniqueId()) &&
                !island.hasPermission(player.getUniqueId(), "SPAWNER_MANAGE")) {
            event.setCancelled(true);
            player.sendMessage("§cNie masz uprawnień do niszczenia spawnerów!");
            return;
        }

        plugin.getSpawnerManager().removeSpawner(block.getLocation());

        // Drop pustego spawnera
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawner.getItemMeta();
        meta.setDisplayName("§6Pusty Spawner");
        spawner.setItemMeta(meta);

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation(), spawner);
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Anuluj interakcję jajkami spawnera
        if (item != null && item.getType().name().endsWith("_SPAWN_EGG")) {
            event.setCancelled(true);
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        if (island == null) return;

        if (!island.getOwnerId().equals(player.getUniqueId()) &&
                !island.hasPermission(player.getUniqueId(), "SPAWNER_SETTINGS")) {
            event.setCancelled(true);
            player.sendMessage("§cNie masz uprawnień do zarządzania spawnerami!");
            return;
        }

        event.setCancelled(true);
        CustomSpawner spawner = plugin.getSpawnerManager().getSpawner(block.getLocation());
        if (spawner != null) {
            new SpawnerGUI(plugin, player, spawner).open();
        }
    }
}