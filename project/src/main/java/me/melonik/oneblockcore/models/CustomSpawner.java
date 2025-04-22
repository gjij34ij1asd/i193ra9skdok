package me.melonik.oneblockcore.models;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class CustomSpawner {
    private Location location;
    private EntityType entityType;
    private int speedLevel;
    private long lastSpawnTime;
    private boolean enabled;

    public CustomSpawner(Location location) {
        this.location = location;
        this.entityType = null;
        this.speedLevel = 0;
        this.lastSpawnTime = System.currentTimeMillis();
        this.enabled = true;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public long getLastSpawnTime() {
        return lastSpawnTime;
    }

    public void setLastSpawnTime(long lastSpawnTime) {
        this.lastSpawnTime = lastSpawnTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMobsPerMinute() {
        switch (speedLevel) {
            case 0: return 50;
            case 1: return 70;
            case 2: return 100;
            case 3: return 200;
            case 4: return 500;
            case 5: return 1000;
            case 6: return 2000;
            case 7: return 3000;
            case 8: return 4000;
            case 9: return 5000;
            case 10: return 10000;
            default: return 50;
        }
    }

    public long getSpawnDelay() {
        return 60000L / getMobsPerMinute();
    }

    public static int getSpeedUpgradeCost(int currentLevel) {
        switch (currentLevel) {
            case 0: return 0;
            case 1: return 5000;
            case 2: return 7000;
            case 3: return 15000;
            case 4: return 20000;
            case 5: return 50000;
            case 6: return 150000;
            case 7: return 5000000;
            case 8: return 1000000;
            case 9: return 5000000;
            case 10: return 20000000;
            default: return -1;
        }
    }

    public static int getMobUpgradeCost(EntityType type) {
        switch (type) {
            case PIG: return 10000;
            case SHEEP: return 30000;
            case COW: return 50000;
            case SPIDER: return 100000;
            case SKELETON: return 140000;
            case ZOMBIE: return 200000;
            case CREEPER: return 500000;
            default: return -1;
        }
    }
}