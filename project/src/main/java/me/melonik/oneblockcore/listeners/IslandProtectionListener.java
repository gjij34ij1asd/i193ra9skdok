package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.block.Container;

public class IslandProtectionListener implements Listener {
    private final Main plugin;

    public IslandProtectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!block.getWorld().getName().equals("oneblock")) {
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        if (island == null) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz niszczyć bloków poza wyspą!", 10, 40, 20);
            return;
        }

        if (island.getOwnerId().equals(player.getUniqueId())) {
            if (block.getType() == Material.HOPPER) {
                island.getUpgrades().decrementHopperCount();
            }
            return;
        }

        if (!island.getMembers().contains(player.getUniqueId()) && !island.getCoopPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz niszczyć bloków na tej wyspie!", 10, 40, 20);
            return;
        }

        String permission = "BREAK";
        if (block.getType() == Material.BEACON) {
            permission = "BEACON_BREAK";
        } else if (block.getType() == Material.SPAWNER) {
            permission = "SPAWNER_MANAGE";
        }

        if (!island.hasPermission(player.getUniqueId(), permission)) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie masz permisji do niszczenia bloków na tej wyspie!", 10, 40, 20);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!block.getWorld().getName().equals("oneblock")) {
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        if (island == null) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz stawiać bloków poza wyspą!", 10, 40, 20);
            return;
        }

        boolean exceedsLimit = false;
        String limitMessage = "";

        switch (block.getType()) {
            case SPAWNER:
                if (island.getUpgrades().getSpawnerCount() >= island.getUpgrades().getSpawnerLimit()) {
                    exceedsLimit = true;
                    limitMessage = "spawnerów (" + island.getUpgrades().getSpawnerCount() + "/" +
                            island.getUpgrades().getSpawnerLimit() + ")";
                }
                break;
            case HOPPER:
                if (island.getUpgrades().getHopperCount() >= island.getUpgrades().getHopperLimit()) {
                    exceedsLimit = true;
                    limitMessage = "lejków (" + island.getUpgrades().getHopperCount() + "/" +
                            island.getUpgrades().getHopperLimit() + ")";
                }
                break;
            case PISTON:
            case STICKY_PISTON:
                if (island.getUpgrades().getPistonCount() >= island.getUpgrades().getPistonLimit()) {
                    exceedsLimit = true;
                    limitMessage = "pistonów (" + island.getUpgrades().getPistonCount() + "/" +
                            island.getUpgrades().getPistonLimit() + ")";
                }
                break;
        }

        if (exceedsLimit) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!","§cOsiągnięto limit §e" + limitMessage + "§c na tej wyspie!", 10, 40, 20);
            return;
        }

        if (island.getOwnerId().equals(player.getUniqueId())) {
            switch (block.getType()) {
                case HOPPER:
                    island.getUpgrades().incrementHopperCount();
                    break;
            }
            return;
        }

        if (!island.getMembers().contains(player.getUniqueId()) && !island.getCoopPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie masz permisji do stawiania bloków na tej wyspie!", 10, 40, 20);
            return;
        }

        String permission = "BUILD";
        if (block.getType() == Material.SPAWNER) {
            permission = "SPAWNER_MANAGE";
        }

        if (!island.hasPermission(player.getUniqueId(), permission)) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie masz permisji do budowania na tej wyspie!", 10, 40, 20);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!block.getWorld().getName().equals("oneblock")) {
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        if (island == null) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz wchodzić w interakcje poza wyspą!", 10, 40, 20);
            return;
        }

        if (island.getOwnerId().equals(player.getUniqueId())) {
            return;
        }

        if (!island.getMembers().contains(player.getUniqueId()) && !island.getCoopPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz wchodzić w interakcje z blokami na tej wyspie!", 10, 40, 20);
            return;
        }

        String permission = null;
        if (block.getState() instanceof Container) {
            permission = "CHEST";
        } else if (block.getType() == Material.FURNACE) {
            permission = "FURNACE";
        } else if (block.getType().name().contains("DOOR") ||
                block.getType().name().contains("GATE") ||
                block.getType().name().contains("TRAPDOOR")) {
            permission = "DOORS";
        } else if (block.getType().name().contains("BUTTON") ||
                block.getType().name().contains("PLATE") ||
                block.getType() == Material.LEVER) {
            permission = "REDSTONE";
        } else if (block.getType() == Material.SPAWNER) {
            permission = "SPAWNER_SETTINGS";
        } else if (block.getType() == Material.BLAST_FURNACE) {
            permission = "GENERATOR";
        }

        if (permission != null && !island.hasPermission(player.getUniqueId(), permission)) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz tego używać na tej wyspie!", 10, 40, 20);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();

        if (!event.getEntity().getWorld().getName().equals("oneblock")) {
            return;
        }

        Island island = plugin.getIslandManager().getIslandAt(event.getEntity().getLocation());
        if (island == null) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz atakować mobów poza wyspą!", 10, 40, 20);
            return;
        }

        if (island.getOwnerId().equals(player.getUniqueId())) {
            return;
        }

        if (!island.getMembers().contains(player.getUniqueId()) && !island.getCoopPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz atakować mobów na tej wyspie!", 10, 40, 20);
            return;
        }

        String permission = null;
        if (event.getEntity() instanceof Monster) {
            permission = "ATTACK_MOBS";
        } else if (event.getEntity() instanceof Animals) {
            permission = "ATTACK_ANIMALS";
        }

        if (permission != null && !island.hasPermission(player.getUniqueId(), permission)) {
            event.setCancelled(true);
            player.sendTitle("§4§lBłąd!", "§cNie możesz permisji do atakowania na tej wyspie!", 10, 40, 20);
        }
    }
}