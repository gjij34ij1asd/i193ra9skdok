package me.melonik.oneblockcore.managers;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager {
    private final Main plugin;
    private final Map<UUID, Island> islands;
    private final Map<UUID, UUID> playerIslands;
    private final Map<UUID, UUID> invitations;
    private final Map<UUID, Long> islandCooldowns;
    private final Map<UUID, Long> deleteCooldowns;
    private final Map<UUID, Integer> deleteConfirmations;
    private final Set<Location> usedLocations;
    private World islandWorld;

    public IslandManager(Main plugin) {
        this.plugin = plugin;
        this.islands = new ConcurrentHashMap<>();
        this.playerIslands = new ConcurrentHashMap<>();
        this.invitations = new ConcurrentHashMap<>();
        this.islandCooldowns = new ConcurrentHashMap<>();
        this.deleteCooldowns = new ConcurrentHashMap<>();
        this.deleteConfirmations = new ConcurrentHashMap<>();
        this.usedLocations = new HashSet<>();
        setupWorld();
        startBorderTask();
        startBlockCountTask();
    }

    private void startBlockCountTask() {
        // Aktualizuj liczniki bloków co 5 minut
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Island island : islands.values()) {
                updateBlockCounts(island);
            }
        }, 6000L, 6000L); // 5 minut = 6000 ticków
    }

    public void updateBlockCounts(Island island) {
        Location center = island.getCenter();
        int radius = island.getBorderSize() / 2;
        int minY = center.getWorld().getMinHeight();
        int maxY = center.getWorld().getMaxHeight();

        int spawnerCount = 0;
        int hopperCount = 0;
        int pistonCount = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y - center.getY(), z);
                    Block block = loc.getBlock();

                    switch (block.getType()) {
                        case SPAWNER:
                            spawnerCount++;
                            break;
                        case HOPPER:
                            hopperCount++;
                            break;
                        case PISTON:
                        case STICKY_PISTON:
                            pistonCount++;
                            break;
                    }
                }
            }
        }

        // Aktualizuj liczniki w obiekcie Island
        island.getUpgrades().setSpawnerCount(spawnerCount);
        island.getUpgrades().setHopperCount(hopperCount);
        island.getUpgrades().setPistonCount(pistonCount);
    }

    public boolean canAddMember(Island island) {
        return island.getMembers().size() < island.getUpgrades().getMemberLimit();
    }

    private void setupWorld() {
        islandWorld = plugin.getServer().getWorld("oneblock");
        if (islandWorld == null) {
            WorldCreator creator = new WorldCreator("oneblock");
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creator.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
            islandWorld = creator.createWorld();

            if (islandWorld != null) {
                islandWorld.setSpawnLocation(0, 64, 0);
                islandWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                islandWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                islandWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                islandWorld.setTime(6000);
                islandWorld.setDifficulty(Difficulty.NORMAL);
            }
        }
    }

    private void startBorderTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Island island : islands.values()) {
                updateIslandBorder(island);
            }
        }, 20L, 20L);
    }

    private void updateIslandBorder(Island island) {
        Location center = island.getCenter();
        int radius = island.getBorderSize() / 2;
        int minY = 0;
        int maxY = center.getWorld().getMaxHeight();

        // Najpierw usuń wszystkie bariery w większym obszarze
        for (int y = minY; y < maxY; y++) {
            for (int x = -radius - 5; x <= radius + 5; x++) {
                for (int z = -radius - 5; z <= radius + 5; z++) {
                    Location loc = center.clone().add(x, y - center.getY(), z);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.BARRIER) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // Teraz ustaw nowe bariery
        for (int y = minY; y < maxY; y++) {
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int z = -radius - 1; z <= radius + 1; z++) {
                    if (Math.abs(x) == radius + 1 || Math.abs(z) == radius + 1) {
                        Location loc = center.clone().add(x, y - center.getY(), z);
                        Block block = loc.getBlock();
                        block.setType(Material.BARRIER);
                    }
                }
            }
        }
    }

    public Island createIsland(Player player) {
        if (hasIsland(player.getUniqueId())) {
            return null;
        }

        // Sprawdź cooldown
        long cooldown = islandCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - cooldown < 1800000) { // 30 minut
            player.sendMessage("§cMusisz poczekać jeszcze " +
                    formatTime((1800000 - (System.currentTimeMillis() - cooldown)) / 1000) +
                    " przed założeniem nowej wyspy!");
            return null;
        }

        if (islandWorld == null) {
            plugin.getLogger().severe("Świat oneblock nie istnieje!");
            return null;
        }

        Location islandLocation = findNextIslandLocation();
        if (islandLocation == null) {
            player.sendMessage("§cNie można znaleźć wolnego miejsca na wyspę!");
            return null;
        }

        Island island = new Island(player.getUniqueId(), islandLocation);

        islands.put(island.getIslandId(), island);
        playerIslands.put(player.getUniqueId(), island.getIslandId());
        usedLocations.add(islandLocation);
        islandCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        generateInitialStructure(islandLocation);

        return island;
    }

    private Location findNextIslandLocation() {
        int islandCount = islands.size();
        int gridSize = plugin.getConfigManager().getIslandSpacing();
        int height = plugin.getConfigManager().getIslandHeight();

        int x = (islandCount % 10) * gridSize;
        int z = (islandCount / 10) * gridSize;

        Location loc = new Location(islandWorld, x, height, z);

        // Sprawdź czy lokacja jest wolna
        if (!usedLocations.contains(loc)) {
            return loc;
        }

        // Jeśli nie, szukaj następnej wolnej lokacji
        for (int i = 0; i < 1000; i++) {
            x = (i % 10) * gridSize;
            z = (i / 10) * gridSize;
            loc = new Location(islandWorld, x, height, z);

            if (!usedLocations.contains(loc)) {
                return loc;
            }
        }

        return null;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void generateInitialStructure(Location location) {
        Block block = location.getBlock();
        block.setType(Material.GRASS_BLOCK);

        BlockData blockData = block.getBlockData();
        block.setBlockData(blockData, false);
    }

    public boolean hasIsland(UUID playerUuid) {
        return playerIslands.containsKey(playerUuid);
    }

    public Island getIsland(UUID islandId) {
        return islands.get(islandId);
    }

    public Island getPlayerIsland(UUID playerUuid) {
        UUID islandId = playerIslands.get(playerUuid);
        return islandId != null ? islands.get(islandId) : null;
    }

    public Island getIslandAt(Location location) {
        if (location.getWorld() != islandWorld) return null;

        int gridSize = plugin.getConfigManager().getIslandSpacing();
        for (Island island : islands.values()) {
            Location center = island.getCenter();
            int radius = island.getBorderSize() / 2;

            if (Math.abs(location.getBlockX() - center.getBlockX()) <= radius &&
                    Math.abs(location.getBlockZ() - center.getBlockZ()) <= radius) {
                return island;
            }
        }
        return null;
    }

    public void deleteIsland(UUID islandId) {
        Island island = islands.get(islandId);
        if (island != null) {
            UUID ownerId = island.getOwnerId();
            Player owner = Bukkit.getPlayer(ownerId);

            // Sprawdź cooldown
            long cooldown = deleteCooldowns.getOrDefault(ownerId, 0L);
            if (System.currentTimeMillis() - cooldown < 1800000) { // 30 minut
                if (owner != null) {
                    owner.sendMessage("§cMusisz poczekać jeszcze " +
                            formatTime((1800000 - (System.currentTimeMillis() - cooldown)) / 1000) +
                            " przed usunięciem wyspy!");
                }
                return;
            }

            // Sprawdź potwierdzenie
            int confirmCount = deleteConfirmations.getOrDefault(ownerId, 0);
            if (confirmCount < 2) {
                if (owner != null) {
                    owner.sendTitle("§4§lUwaga!", "§cZobacz Chat!", 10, 40, 10);
                    owner.sendMessage("");
                    owner.sendMessage("§c§lUWAGA!");
                    owner.sendMessage("§cAby potwierdzić usunięcie wyspy, wpisz komendę jeszcze " + (2 - confirmCount) + " raz" + ((2 - confirmCount) == 1 ? "!" : "y!"));
                    owner.sendMessage("§cMasz na to 60 sekund!");
                    owner.sendMessage("");

                    // Teleportuj gracza na spawn świata "world" przy pierwszym potwierdzeniu
                    if (confirmCount == 0) {
                        World mainWorld = Bukkit.getWorld("world");
                        if (mainWorld != null && owner != null) {
                            owner.teleport(mainWorld.getSpawnLocation());
                        }
                    }
                }

                deleteConfirmations.put(ownerId, confirmCount + 1);

                // Usuń potwierdzenie po 60 sekundach
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    deleteConfirmations.remove(ownerId);
                }, 1200L); // 60 sekund
                return;
            }

            // Usuń bariery przed usunięciem wyspy
            clearOldBorder(island.getCenter(), island.getBorderSize() / 2 + 1, 0, island.getCenter().getWorld().getMaxHeight());

            // Usuń właściciela z mapy playerIslands
            playerIslands.remove(ownerId);

            // Usuń wszystkich członków z mapy playerIslands
            for (UUID memberId : new ArrayList<>(island.getMembers())) {
                playerIslands.remove(memberId);
            }

            // Usuń wyspę z mapy islands
            islands.remove(islandId);
            usedLocations.remove(island.getCenter());
            deleteCooldowns.put(ownerId, System.currentTimeMillis());
            deleteConfirmations.remove(ownerId);
        }
    }

    private void clearOldBorder(Location center, int radius, int minY, int maxY) {
        for (int y = minY; y < maxY; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        Location loc = center.clone().add(x, y - center.getY(), z);
                        Block block = loc.getBlock();
                        if (block.getType() == Material.BARRIER) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    public void invitePlayer(UUID islandId, UUID targetPlayer) {
        invitations.put(targetPlayer, islandId);
    }

    public UUID getInvitation(UUID playerUuid) {
        return invitations.get(playerUuid);
    }

    public void removeInvitation(UUID playerUuid) {
        invitations.remove(playerUuid);
    }

    public void addMember(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            if (canAddMember(island)) {
                island.getMembers().add(playerUuid);
                playerIslands.put(playerUuid, islandId);
                removeInvitation(playerUuid);
            }
        }
    }

    public void removeMember(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.getMembers().remove(playerUuid);
            playerIslands.remove(playerUuid);
        }
    }

    public void addCoop(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.getCoopPlayers().add(playerUuid);
        }
    }

    public void removeCoop(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.getCoopPlayers().remove(playerUuid);
        }
    }

    public void banPlayer(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.getBanned().add(playerUuid);
            if (island.getMembers().contains(playerUuid)) {
                removeMember(islandId, playerUuid);
            }
        }
    }

    public void unbanPlayer(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        if (island != null) {
            island.getBanned().remove(playerUuid);
        }
    }

    public boolean isBanned(UUID islandId, UUID playerUuid) {
        Island island = islands.get(islandId);
        return island != null && island.getBanned().contains(playerUuid);
    }

    public Map<UUID, Island> getIslands() {
        return islands;
    }

    public void setIslands(Map<UUID, Island> newIslands) {
        this.islands.clear();
        this.islands.putAll(newIslands);

        // Odbuduj usedLocations
        this.usedLocations.clear();
        for (Island island : newIslands.values()) {
            this.usedLocations.add(island.getCenter());
        }
    }

    public void setPlayerIslands(Map<UUID, UUID> newPlayerIslands) {
        this.playerIslands.clear();
        this.playerIslands.putAll(newPlayerIslands);
    }

    public void loadIslands() {
        plugin.getDatabaseManager().loadData();
    }

    public void saveIslands() {
        plugin.getDatabaseManager().saveData();
    }
}