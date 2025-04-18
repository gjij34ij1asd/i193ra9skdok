package me.melonik.oneblockcore.managers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.CustomSpawner;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final Main plugin;
    private final Gson gson;
    private final File dataFolder;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.gson = createGson();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, new JsonSerializer<Location>() {
                    @Override
                    public JsonElement serialize(Location location, java.lang.reflect.Type type, JsonSerializationContext context) {
                        JsonObject object = new JsonObject();
                        object.addProperty("world", location.getWorld().getName());
                        object.addProperty("x", location.getX());
                        object.addProperty("y", location.getY());
                        object.addProperty("z", location.getZ());
                        object.addProperty("yaw", location.getYaw());
                        object.addProperty("pitch", location.getPitch());
                        return object;
                    }
                })
                .registerTypeAdapter(Location.class, new JsonDeserializer<Location>() {
                    @Override
                    public Location deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject object = json.getAsJsonObject();
                        return new Location(
                                Bukkit.getWorld(object.get("world").getAsString()),
                                object.get("x").getAsDouble(),
                                object.get("y").getAsDouble(),
                                object.get("z").getAsDouble(),
                                object.get("yaw").getAsFloat(),
                                object.get("pitch").getAsFloat()
                        );
                    }
                })
                .registerTypeAdapter(UUID.class, new JsonSerializer<UUID>() {
                    @Override
                    public JsonElement serialize(UUID uuid, java.lang.reflect.Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(uuid.toString());
                    }
                })
                .registerTypeAdapter(UUID.class, new JsonDeserializer<UUID>() {
                    @Override
                    public UUID deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
                        return UUID.fromString(json.getAsString());
                    }
                })
                .registerTypeAdapter(EntityType.class, new JsonSerializer<EntityType>() {
                    @Override
                    public JsonElement serialize(EntityType entityType, java.lang.reflect.Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(entityType.name());
                    }
                })
                .registerTypeAdapter(EntityType.class, new JsonDeserializer<EntityType>() {
                    @Override
                    public EntityType deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
                        return EntityType.valueOf(json.getAsString());
                    }
                })
                .serializeNulls()
                .create();
    }

    public void saveData() {
        saveIslands();
        saveEconomy();
        saveSpawners();
    }

    public void loadData() {
        loadIslands();
        loadEconomy();
        loadSpawners();
    }

    private void saveSpawners() {
        File spawnersFile = new File(dataFolder, "spawners.json");
        try (Writer writer = new FileWriter(spawnersFile)) {
            Map<String, CustomSpawner> spawners = new HashMap<>();
            plugin.getSpawnerManager().getSpawners().forEach((loc, spawner) ->
                    spawners.put(locationToString(loc), spawner));
            gson.toJson(spawners, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Błąd podczas zapisywania spawnerów: " + e.getMessage());
        }
    }

    private void loadSpawners() {
        File spawnersFile = new File(dataFolder, "spawners.json");
        if (!spawnersFile.exists()) return;

        try (Reader reader = new FileReader(spawnersFile)) {
            Type type = new TypeToken<Map<String, CustomSpawner>>(){}.getType();
            Map<String, CustomSpawner> spawners = gson.fromJson(reader, type);

            if (spawners != null) {
                Map<Location, CustomSpawner> convertedSpawners = new HashMap<>();
                spawners.forEach((locStr, spawner) -> {
                    Location loc = stringToLocation(locStr);
                    if (loc != null) {
                        convertedSpawners.put(loc, spawner);
                    }
                });
                plugin.getSpawnerManager().setSpawners(convertedSpawners);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd podczas wczytywania spawnerów: " + e.getMessage());
        }
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ();
    }

    private Location stringToLocation(String str) {
        try {
            String[] parts = str.split(",");
            return new Location(
                    Bukkit.getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void saveIslands() {
        File islandsFile = new File(dataFolder, "islands.json");
        try (Writer writer = new FileWriter(islandsFile)) {
            Map<UUID, Island> islands = plugin.getIslandManager().getIslands();
            gson.toJson(islands, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Błąd podczas zapisywania wysp: " + e.getMessage());
        }
    }

    private void loadIslands() {
        File islandsFile = new File(dataFolder, "islands.json");
        if (!islandsFile.exists()) return;

        try (Reader reader = new FileReader(islandsFile)) {
            Type type = new TypeToken<Map<UUID, Island>>(){}.getType();
            Map<UUID, Island> islands = gson.fromJson(reader, type);

            if (islands != null) {
                plugin.getIslandManager().setIslands(islands);

                // Rebuild playerIslands map
                Map<UUID, UUID> playerIslands = new HashMap<>();
                for (Map.Entry<UUID, Island> entry : islands.entrySet()) {
                    Island island = entry.getValue();
                    playerIslands.put(island.getOwnerId(), island.getIslandId());

                    // Add members to playerIslands map
                    for (UUID memberId : island.getMembers()) {
                        playerIslands.put(memberId, island.getIslandId());
                    }
                }
                plugin.getIslandManager().setPlayerIslands(playerIslands);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Błąd podczas wczytywania wysp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveEconomy() {
        if (!plugin.isVaultEnabled()) {
            File economyFile = new File(dataFolder, "economy.json");
            try (Writer writer = new FileWriter(economyFile)) {
                Map<String, Map<String, Double>> economyData = new HashMap<>();

                Map<String, Double> playerMoneyMap = new HashMap<>();
                plugin.getEconomyManager().getPlayerMoneyMap().forEach((uuid, amount) ->
                        playerMoneyMap.put(uuid.toString(), amount));

                Map<String, Double> bankMoneyMap = new HashMap<>();
                plugin.getEconomyManager().getBankMoneyMap().forEach((uuid, amount) ->
                        bankMoneyMap.put(uuid.toString(), amount));

                economyData.put("playerMoney", playerMoneyMap);
                economyData.put("bankMoney", bankMoneyMap);

                gson.toJson(economyData, writer);
            } catch (IOException e) {
                plugin.getLogger().severe("Błąd podczas zapisywania ekonomii: " + e.getMessage());
            }
        }
    }

    private void loadEconomy() {
        if (!plugin.isVaultEnabled()) {
            File economyFile = new File(dataFolder, "economy.json");
            if (!economyFile.exists()) return;

            try (Reader reader = new FileReader(economyFile)) {
                Type type = new TypeToken<Map<String, Map<String, Double>>>(){}.getType();
                Map<String, Map<String, Double>> economyData = gson.fromJson(reader, type);
                if (economyData != null) {
                    Map<String, Map<UUID, Double>> convertedData = new HashMap<>();

                    economyData.forEach((key, value) -> {
                        Map<UUID, Double> convertedMap = new HashMap<>();
                        value.forEach((uuidStr, amount) ->
                                convertedMap.put(UUID.fromString(uuidStr), amount));
                        convertedData.put(key, convertedMap);
                    });

                    plugin.getEconomyManager().loadFromMap(convertedData);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Błąd podczas wczytywania ekonomii: " + e.getMessage());
            }
        }
    }
}