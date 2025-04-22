package me.melonik.oneblockcore.managers;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.CustomSpawner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnerManager {
    private final Main plugin;
    private final Map<Location, CustomSpawner> spawners;
    private final Map<CustomSpawner, Integer> remainingSpawns;
    private final Random random = new Random();

    public SpawnerManager(Main plugin) {
        this.plugin = plugin;
        this.spawners = new HashMap<>();
        this.remainingSpawns = new HashMap<>();
        startSpawnerTask();
        startResetTask();
    }

    private void startSpawnerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (CustomSpawner spawner : spawners.values()) {
                    if (!spawner.isEnabled() || spawner.getEntityType() == null) continue;

                    long timeSinceLastSpawn = currentTime - spawner.getLastSpawnTime();
                    if (timeSinceLastSpawn >= spawner.getSpawnDelay()) {
                        int mobsToSpawn = Math.min(4, getRemainingSpawns(spawner));
                        if (mobsToSpawn > 0) {
                            spawnMobs(spawner, mobsToSpawn);
                            spawner.setLastSpawnTime(currentTime);
                            decreaseRemainingSpawns(spawner, mobsToSpawn);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (CustomSpawner spawner : spawners.values()) {
                    remainingSpawns.put(spawner, spawner.getMobsPerMinute());
                }
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Reset co minutę (1200 ticks = 60 sekund)
    }

    private int getRemainingSpawns(CustomSpawner spawner) {
        return remainingSpawns.getOrDefault(spawner, spawner.getMobsPerMinute());
    }

    private void decreaseRemainingSpawns(CustomSpawner spawner, int amount) {
        remainingSpawns.put(spawner, Math.max(0, getRemainingSpawns(spawner) - amount));
    }

    private void spawnMobs(CustomSpawner spawner, int count) {
        List<Location> safeLocations = findSafeLocations(spawner.getLocation(), count);
        for (Location loc : safeLocations) {
            if (loc != null) {
                loc.getWorld().spawnEntity(loc, spawner.getEntityType());
            }
        }
    }

    private List<Location> findSafeLocations(Location center, int count) {
        List<Location> locations = new ArrayList<>();
        World world = center.getWorld();
        int radius = 5;

        for (int attempts = 0; attempts < count * 3 && locations.size() < count; attempts++) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;

            Location loc = center.clone().add(x, 0, z);

            Block block = null;
            for (int y = 0; y <= 3; y++) {
                block = loc.clone().subtract(0, y, 0).getBlock();
                if (!block.isEmpty() && !block.isLiquid()) {
                    loc = block.getLocation().add(0.5, 1, 0.5);
                    break;
                }
            }

            if (block != null && !block.isEmpty() && !block.isLiquid()) {
                Location spawnLoc = loc.clone();
                if (spawnLoc.getBlock().isEmpty() &&
                        spawnLoc.clone().add(0, 1, 0).getBlock().isEmpty() &&
                        !isLocationTooClose(spawnLoc, locations)) {
                    locations.add(spawnLoc);
                }
            }
        }

        return locations;
    }

    private boolean isLocationTooClose(Location loc, List<Location> existingLocations) {
        for (Location existing : existingLocations) {
            if (existing.distance(loc) < 1.5) {
                return true;
            }
        }
        return false;
    }

    public CustomSpawner getSpawner(Location location) {
        return spawners.get(location);
    }

    public void addSpawner(Location location) {
        CustomSpawner spawner = new CustomSpawner(location);
        spawners.put(location, spawner);
        remainingSpawns.put(spawner, spawner.getMobsPerMinute());
    }

    public void removeSpawner(Location location) {
        CustomSpawner spawner = spawners.remove(location);
        if (spawner != null) {
            remainingSpawns.remove(spawner);
        }
    }

    public Map<Location, CustomSpawner> getSpawners() {
        return spawners;
    }

    public void setSpawners(Map<Location, CustomSpawner> spawners) {
        this.spawners.clear();
        this.spawners.putAll(spawners);

        for (CustomSpawner spawner : spawners.values()) {
            remainingSpawns.put(spawner, spawner.getMobsPerMinute());
        }
    }
}