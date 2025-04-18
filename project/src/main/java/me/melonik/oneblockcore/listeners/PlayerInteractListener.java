package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.gui.GeneratorGUI;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    private final Main plugin;

    public PlayerInteractListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());

        if (island != null && block.getLocation().equals(island.getCenter())) {
            event.setCancelled(true);

            // Sprawdź czy blok jest generatorem (GRASS_BLOCK dla poziomu 1 lub BLAST_FURNACE dla poziomu 7)
            Material blockType = block.getType();
            if (blockType == Material.BLAST_FURNACE) {

                if (island.getOwnerId().equals(player.getUniqueId()) ||
                        island.hasPermission(player.getUniqueId(), "GENERATOR")) {
                    new GeneratorGUI(plugin, player, island).open();
                } else {
                    player.sendMessage("§cNie masz uprawnień do używania generatora!");
                }
            }
        }
    }
}