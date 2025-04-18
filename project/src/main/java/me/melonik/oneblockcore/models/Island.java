package me.melonik.oneblockcore.models;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Island {
    private final UUID islandId;
    private UUID ownerId;
    private Location center;
    private Location spawnLocation;
    private final Set<UUID> members;
    private final Set<UUID> coopPlayers;
    private final Set<UUID> banned;
    private final Map<UUID, Map<String, Boolean>> permissions;
    private final Map<String, Boolean> coopPermissions;
    private final Generator generator;
    private final IslandUpgrades upgrades;
    private int maxLevel;
    private int borderSize;
    private boolean pickupItems;
    private boolean pvp;
    private boolean mobGriefing;
    private boolean alwaysDay;
    private boolean animalDamage;
    private boolean mobDamage;
    private boolean visitable;

    public Island(UUID ownerId, Location center) {
        this.islandId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.center = center;
        this.members = new HashSet<>();
        this.coopPlayers = new HashSet<>();
        this.banned = new HashSet<>();
        this.permissions = new HashMap<>();
        this.coopPermissions = new HashMap<>();
        this.generator = new Generator();
        this.upgrades = new IslandUpgrades();
        this.maxLevel = 1;
        this.borderSize = 50;
        this.pickupItems = true;
        this.pvp = false;
        this.mobGriefing = false;
        this.alwaysDay = true;
        this.animalDamage = false;
        this.mobDamage = true;
        this.visitable = true;
        initializeDefaultCoopPermissions();
    }

    private void initializeDefaultCoopPermissions() {
        coopPermissions.put("BREAK", false);
        coopPermissions.put("BUILD", false);
        coopPermissions.put("BEACON_BREAK", false);
        coopPermissions.put("CHEST", false);
        coopPermissions.put("FURNACE", false);
        coopPermissions.put("ATTACK_MOBS", false);
        coopPermissions.put("ATTACK_ANIMALS", false);
        coopPermissions.put("DOORS", false);
        coopPermissions.put("UPGRADES", false);
        coopPermissions.put("REDSTONE", false);
        coopPermissions.put("SPAWNER_MANAGE", false);
        coopPermissions.put("SPAWNER_SETTINGS", false);
        coopPermissions.put("GENERATOR", false);
    }

    public UUID getIslandId() {
        return islandId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getCoopPlayers() {
        return coopPlayers;
    }

    public Set<UUID> getBanned() {
        return banned;
    }

    public Generator getGenerator() {
        return generator;
    }

    public IslandUpgrades getUpgrades() {
        return upgrades;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public boolean isPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(boolean pickupItems) {
        this.pickupItems = pickupItems;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isMobGriefing() {
        return mobGriefing;
    }

    public void setMobGriefing(boolean mobGriefing) {
        this.mobGriefing = mobGriefing;
    }

    public boolean isAlwaysDay() {
        return alwaysDay;
    }

    public void setAlwaysDay(boolean alwaysDay) {
        this.alwaysDay = alwaysDay;
    }

    public boolean isAnimalDamage() {
        return animalDamage;
    }

    public void setAnimalDamage(boolean animalDamage) {
        this.animalDamage = animalDamage;
    }

    public boolean isMobDamage() {
        return mobDamage;
    }

    public void setMobDamage(boolean mobDamage) {
        this.mobDamage = mobDamage;
    }

    public boolean isVisitable() {
        return visitable;
    }

    public void setVisitable(boolean visitable) {
        this.visitable = visitable;
    }

    public boolean hasPermission(UUID playerId, String permission) {
        if (playerId.equals(ownerId)) return true;
        Map<String, Boolean> playerPerms = permissions.get(playerId);
        return playerPerms != null && playerPerms.getOrDefault(permission, false);
    }

    public void setPermission(UUID playerId, String permission, boolean value) {
        permissions.computeIfAbsent(playerId, k -> new HashMap<>()).put(permission, value);
    }

    public boolean getCoopPermission(String permission) {
        return coopPermissions.getOrDefault(permission, false);
    }

    public void setCoopPermission(String permission, boolean value) {
        coopPermissions.put(permission, value);
    }

    public boolean isOnIsland(Player player) {
        Location playerLoc = player.getLocation();
        int radius = borderSize / 2;
        return Math.abs(playerLoc.getBlockX() - center.getBlockX()) <= radius &&
                Math.abs(playerLoc.getBlockZ() - center.getBlockZ()) <= radius;
    }
}