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
        return "ZacolecOneBlock";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        if (params.equals("has_island")) {
            return plugin.getIslandManager().hasIsland(player.getUniqueId()) ? "1" : "0";
        }

        if (params.equals("generator_level")) {
            Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
            return island != null ? String.valueOf(island.getMaxLevel()) : "0";
        }

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

        if (params.equals("money")) {
            return formatMoney(plugin.getEconomyManager().getPlayerMoney(player.getUniqueId()));
        }

        if (params.equals("bank")) {
            return formatMoney(plugin.getEconomyManager().getBankMoney(player.getUniqueId()));
        }

        if (params.startsWith("money_top_")) {
            try {
                int position = Integer.parseInt(params.substring(10)) - 1;
                Map.Entry<UUID, Double> entry = plugin.getEconomyManager().getMoneyTopList(position + 1).get(position);
                return formatMoney(entry.getValue());
            } catch (Exception e) {
                return "0.0";
            }
        }

        if (params.startsWith("bank_top_")) {
            try {
                int position = Integer.parseInt(params.substring(9)) - 1;
                Map.Entry<UUID, Double> entry = plugin.getEconomyManager().getBankTopList(position + 1).get(position);
                return formatMoney(entry.getValue());
            } catch (Exception e) {
                return "0.0";
            }
        }

        return null;
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1fmld", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fmln", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format("%.1fk", amount / 1_000);
        } else {
            return String.format("%.1f", amount);
        }
    }
}