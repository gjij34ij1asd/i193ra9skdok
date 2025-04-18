package me.melonik.oneblockcore.listeners;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Generator;
import me.melonik.oneblockcore.models.Island;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BlockBreakListener implements Listener {
    private final Main plugin;

    public BlockBreakListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Island island = plugin.getIslandManager().getIslandAt(block.getLocation());
        if (island == null) {
            event.setCancelled(true);
            player.sendMessage("§cNie możesz niszczyć bloków poza wyspą!");
            return;
        }

        // Aktualizuj liczniki bloków
        switch (block.getType()) {
            case SPAWNER:
                island.getUpgrades().setSpawnerCount(Math.max(0, island.getUpgrades().getSpawnerCount() - 1));
                break;
            case HOPPER:
                island.getUpgrades().setHopperCount(Math.max(0, island.getUpgrades().getHopperCount() - 1));
                break;
            case PISTON:
            case STICKY_PISTON:
                island.getUpgrades().setPistonCount(Math.max(0, island.getUpgrades().getPistonCount() - 1));
                break;
        }

        if (isGeneratorBlock(block, island)) {
            if (!island.getOwnerId().equals(player.getUniqueId()) && !island.hasPermission(player.getUniqueId(), "BREAK")) {
                event.setCancelled(true);
                player.sendMessage("§cNie masz uprawnień do niszczenia na tej wyspie!");
                return;
            }

            event.setCancelled(true);
            event.setDropItems(false);

            Generator generator = island.getGenerator();
            if (generator != null) {
                if (generator.getLevel() == 7 && block.getType() == Material.BLAST_FURNACE) {
                    player.sendTitle("§4§lBłąd", "§7Zobacz chat", 10, 40, 10);
                    player.sendMessage("§cTego bloku nie możesz wykopać zmień go pod /panel");
                    return;
                }

                if (generator.getSelectedGeneratorLevel() == generator.getLevel()) {
                    int currentProgress = generator.getProgress();
                    int progressIncrease = Math.max(1, 10 - (generator.getSelectedGeneratorLevel() / 2));
                    int newProgress = currentProgress + progressIncrease;

                    if (newProgress >= 100) {
                        if (generator.getSelectedGeneratorLevel() < 7 && currentProgress < 100) {
                            player.sendMessage("");
                            player.sendMessage("§6§lGRATULACJE!");
                            player.sendMessage("§7Twój generator osiągnął maksymalny postęp!");
                            player.sendMessage("§7Możesz go ulepszyć do poziomu §f" + (generator.getSelectedGeneratorLevel() + 1) + " §7w panelu.");
                            player.sendMessage("§7Aby to zrobić, użyj komendy §f/panel §7i wybierz nowy typ generatora.");
                            player.sendMessage("");

                            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);

                            if (generator.getSelectedGeneratorLevel() == generator.getLevel()) {
                                island.setMaxLevel(generator.getLevel() + 1);
                                generator.setLevel(generator.getLevel() + 1);
                                plugin.getIslandManager().saveIslands();
                            }
                        }
                        generator.setProgress(100);
                    } else {
                        generator.setProgress(newProgress);
                    }

                    if (generator.getSelectedGeneratorLevel() != 7) {
                        String progressBar = plugin.getGeneratorManager().getProgressBar(generator.getProgress());
                        String actionBarMessage = String.format("§8[§b%d§8/§7%d§8] §7%s §8>> §7Generator: %s §8[§7%d%%§8]",
                                generator.getSelectedGeneratorLevel(), 7, player.getName(), progressBar, generator.getProgress());

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                            }
                        }.runTaskLater(plugin, 40L);
                    }
                }

                Location dropLocation = block.getLocation().clone().add(0.5, 1, 0.5);
                Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());

                Material newType = plugin.getGeneratorManager().getRandomMaterial(generator.getSelectedGeneratorLevel());
                Material currentType = block.getType();

                if (currentType == Material.AIR) {
                    block.setType(Material.GRASS_BLOCK);
                } else {
                    block.setType(newType);
                }

                if (!drops.isEmpty()) {
                    for (ItemStack drop : drops) {
                        block.getWorld().dropItemNaturally(dropLocation, drop);
                    }
                }

                int expToDrop = getExpFromBlock(currentType);
                if (expToDrop > 0) {
                    event.setExpToDrop(expToDrop);
                    block.getWorld().spawn(dropLocation, org.bukkit.entity.ExperienceOrb.class).setExperience(expToDrop);
                }
            }
        }
    }

    private boolean isGeneratorBlock(Block block, Island island) {
        return block.getLocation().equals(island.getCenter());
    }

    private int getExpFromBlock(Material material) {
        return switch (material) {
            case COAL_ORE -> 0;
            case DEEPSLATE_COAL_ORE -> 0;
            case NETHER_GOLD_ORE -> 1;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> 1;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> 1;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> 1;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 5;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 5;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 4;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> 3;
            case NETHER_QUARTZ_ORE -> 3;
            case ANCIENT_DEBRIS -> 3;
            case SPAWNER -> 15;
            default -> 0;
        };
    }
}