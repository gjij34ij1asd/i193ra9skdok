package me.melonik.oneblockcore.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class OneBlockPlaceholders extends PlaceholderExpansion {
    private final Main plugin;

    public OneBlockPlaceholders(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "oneblock";
    }

    @Override
    public String getAuthor() {
        return "Melonik";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        // %oneblock_has_island%
        if (params.equals("has_island")) {
            return plugin.getIslandManager().hasIsland(player.getUniqueId()) ? "1" : "0";
        }

        // %oneblock_generator_level%
        if (params.equals("generator_level")) {
            Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
            return island != null ? String.valueOf(island.getMaxLevel()) : "0";
        }

        // %oneblock_generator_progress%
        if (params.equals("generator_progress")) {
            Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
            if (island != null) {
                if (island.getGenerator().getSelectedGeneratorLevel() == 7) {
                    return "§8[§bMAKSYMALNY!§8]";
                }
                if (island.getGenerator().getProgress() >= 100 ||
                        island.getGenerator().getSelectedGeneratorLevel() < island.getGenerator().getLevel()) {
                    return "§8[§bZmień pod /panel§8]";
                }
                String progressBar = plugin.getGeneratorManager().getProgressBar(island.getGenerator().getProgress());
                return String.format("§8[%s§8] §b%d%%", progressBar, island.getGenerator().getProgress());
            }
            return "§8[§7●●●●●●●●●●§8] §b0%";
        }

        // %oneblock_money%
        if (params.equals("money")) {
            return String.format("%.2f", plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()));
        }

        // %oneblock_bank%
        if (params.equals("bank")) {
            return String.format("%.2f", plugin.getEconomyManager().getBankMoney(player.getUniqueId()));
        }

        // %oneblock_money_top_X%
        if (params.startsWith("money_top_")) {
            try {
                int position = Integer.parseInt(params.substring(10)) - 1;
                Map.Entry<UUID, Double> entry = plugin.getEconomyManager().getMoneyTopList(position + 1).get(position);
                return String.format("%.2f", entry.getValue());
            } catch (Exception e) {
                return "0.00";
            }
        }

        // %oneblock_bank_top_X%
        if (params.startsWith("bank_top_")) {
            try {
                int position = Integer.parseInt(params.substring(9)) - 1;
                Map.Entry<UUID, Double> entry = plugin.getEconomyManager().getBankTopList(position + 1).get(position);
                return String.format("%.2f", entry.getValue());
            } catch (Exception e) {
                return "0.00";
            }
        }

        return null;
    }
}