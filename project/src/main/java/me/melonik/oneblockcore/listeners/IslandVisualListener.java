package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandVisualListener implements Listener {
    private final Main plugin;
    private final Map<UUID, BossBar> playerBossBars;
    private final Map<UUID, Island> playerCurrentIsland;

    public IslandVisualListener(Main plugin) {
        this.plugin = plugin;
        this.playerBossBars = new HashMap<>();
        this.playerCurrentIsland = new HashMap<>();

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateBorders, 20L, 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Island island = plugin.getIslandManager().getIslandAt(to);
        Island currentIsland = playerCurrentIsland.get(player.getUniqueId());

        if (island != currentIsland) {
            if (currentIsland != null) {
                removeBossBar(player);
                player.setWorldBorder(null);
                player.resetPlayerTime();
            }

            if (island != null) {
                showIslandInfo(player, island);
                updateWorldBorder(player, island);
                updatePlayerTime(player, island);
            }

            playerCurrentIsland.put(player.getUniqueId(), island);
        }
    }

    private void updatePlayerTime(Player player, Island island) {
        if (island.isAlwaysDay()) {
            player.setPlayerTime(6000, false);
        } else {
            player.setPlayerTime(18000, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeBossBar(player);
        playerCurrentIsland.remove(player.getUniqueId());
        player.resetPlayerTime();
    }

    private void showIslandInfo(Player player, Island island) {
        BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);

        if (island.getOwnerId().equals(player.getUniqueId()) || island.getMembers().contains(player.getUniqueId())) {
            bossBar.setTitle("§bJesteś na swojej wyspie");
        } else {
            String ownerName = Bukkit.getOfflinePlayer(island.getOwnerId()).getName();
            bossBar.setTitle("§bJesteś na wyspie gracza §3" + ownerName);
        }

        bossBar.addPlayer(player);
        playerBossBars.put(player.getUniqueId(), bossBar);
    }

    private void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    private void updateWorldBorder(Player player, Island island) {
        org.bukkit.WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(island.getCenter());
        border.setSize(island.getBorderSize());
        border.setWarningDistance(3);
        player.setWorldBorder(border);
    }

    private void updateBorders() {
        for (Map.Entry<UUID, Island> entry : playerCurrentIsland.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Island island = entry.getValue();

            if (player != null && island != null && player.isOnline()) {
                updateWorldBorder(player, island);
            }
        }
    }
}