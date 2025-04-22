package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final Main plugin;

    public PlayerQuitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        for (Island island : plugin.getIslandManager().getIslands().values()) {
            if (island.getCoopPlayers().contains(player.getUniqueId())) {
                island.getCoopPlayers().remove(player.getUniqueId());
                plugin.getIslandManager().saveIslands();
            }
        }
    }
}