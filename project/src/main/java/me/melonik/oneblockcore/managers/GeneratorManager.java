package me.melonik.oneblockcore.managers;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Generator;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class GeneratorManager {
    private final Main plugin;
    private final Map<Integer, List<Material>> levelMaterials;
    private final Map<Integer, String> levelNames;
    private final Set<Material> availableGenerators;

    public GeneratorManager(Main plugin) {
        this.plugin = plugin;
        this.levelMaterials = new HashMap<>();
        this.levelNames = new HashMap<>();
        this.availableGenerators = new HashSet<>();
        initializeLevels();
    }

    private void initializeLevels() {
        // Poziom 1 - Dirt
        List<Material> dirtLevel = Arrays.asList(
                Material.DIRT,
                Material.GRASS_BLOCK,
                Material.COARSE_DIRT
        );
        levelMaterials.put(1, dirtLevel);
        levelNames.put(1, "Dirt");

        // Poziom 2 - Drewno
        List<Material> woodLevel = Arrays.asList(
                Material.OAK_LOG,
                Material.BIRCH_LOG,
                Material.SPRUCE_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.DARK_OAK_LOG
        );
        levelMaterials.put(2, woodLevel);
        levelNames.put(2, "Drewno");

        // Poziom 3 - Kamień
        List<Material> stoneLevel = Arrays.asList(
                Material.STONE,
                Material.COBBLESTONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE
        );
        levelMaterials.put(3, stoneLevel);
        levelNames.put(3, "Kamień");

        // Poziom 4 - Rudy
        List<Material> oreLevel = Arrays.asList(
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.COPPER_ORE
        );
        levelMaterials.put(4, oreLevel);
        levelNames.put(4, "Rudy");

        // Poziom 5 - Nether
        List<Material> netherLevel = Arrays.asList(
                Material.NETHERRACK,
                Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS,
                Material.NETHER_GOLD_ORE,
                Material.CRIMSON_NYLIUM,
                Material.WARPED_NYLIUM
        );
        levelMaterials.put(5, netherLevel);
        levelNames.put(5, "Nether");

        // Poziom 6 - End
        List<Material> endLevel = Arrays.asList(
                Material.END_STONE,
                Material.PURPUR_BLOCK,
                Material.END_STONE_BRICKS,
                Material.CHORUS_FLOWER
        );
        levelMaterials.put(6, endLevel);
        levelNames.put(6, "End");

        // Poziom 7 - Blast Furnace (tylko jeden blok)
        List<Material> blastFurnaceLevel = Collections.singletonList(Material.BLAST_FURNACE);
        levelMaterials.put(7, blastFurnaceLevel);
        levelNames.put(7, "Ultimate");

        // Dostępne generatory
        availableGenerators.add(Material.GRASS_BLOCK); // Poziom 1
        availableGenerators.add(Material.OAK_LOG);    // Poziom 2
        availableGenerators.add(Material.STONE);      // Poziom 3
        availableGenerators.add(Material.DIAMOND_ORE); // Poziom 4
        availableGenerators.add(Material.NETHERRACK);  // Poziom 5
        availableGenerators.add(Material.END_STONE);   // Poziom 6
        availableGenerators.add(Material.BLAST_FURNACE); // Poziom 7
    }

    public Material getRandomMaterial(int level) {
        if (level == 7) {
            return Material.BLAST_FURNACE;
        }

        List<Material> materials = levelMaterials.get(level);
        if (materials == null || materials.isEmpty()) {
            return Material.STONE;
        }
        return materials.get(new Random().nextInt(materials.size()));
    }

    public String getLevelName(int level) {
        return levelNames.getOrDefault(level, "Poziom " + level);
    }

    public String getProgressBar(int progress) {
        StringBuilder bar = new StringBuilder();
        int filledSquares = progress / 10;

        for (int i = 0; i < 10; i++) {
            if (i < filledSquares) {
                bar.append("§b●");
            } else {
                bar.append("§7●");
            }
        }

        return bar.toString();
    }

    public boolean isGeneratorMaterial(Material material) {
        return availableGenerators.contains(material);
    }

    public Set<Material> getAvailableGenerators() {
        return availableGenerators;
    }

    public int getGeneratorLevel(Material material) {
        for (Map.Entry<Integer, List<Material>> entry : levelMaterials.entrySet()) {
            if (entry.getValue().contains(material)) {
                return entry.getKey();
            }
        }
        return 1;
    }
}