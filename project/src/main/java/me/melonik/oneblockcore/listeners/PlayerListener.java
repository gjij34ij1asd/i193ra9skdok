package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerListener implements Listener {
    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Wczytaj dane ekonomiczne gracza
        plugin.getEconomyManager().loadPlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Obsługa utraty pieniędzy przy śmierci
        plugin.getEconomyManager().handlePlayerDeath(player);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Island island = plugin.getIslandManager().getIslandAt(event.getItem().getLocation());

        if (island != null) {
            // Jeśli gracz nie jest właścicielem ani członkiem wyspy
            if (!island.getOwnerId().equals(player.getUniqueId()) &&
                    !island.getMembers().contains(player.getUniqueId())) {

                // Sprawdź ustawienie podnoszenia przedmiotów
                if (!island.isPickupItems()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}