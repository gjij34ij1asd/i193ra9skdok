package me.melonik.oneblockcore.managers;

import me.melonik.oneblockcore.Main;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {
    private final Main plugin;
    private final Map<UUID, Double> playerMoney;
    private final Map<UUID, Double> bankMoney;

    public EconomyManager(Main plugin) {
        this.plugin = plugin;
        this.playerMoney = new ConcurrentHashMap<>();
        this.bankMoney = new ConcurrentHashMap<>();
    }

    public double getPlayerMoney(UUID playerId) {
        if (!plugin.isVaultEnabled()) {
            return playerMoney.getOrDefault(playerId, plugin.getConfigManager().getInitialMoney());
        }
        Player player = plugin.getServer().getPlayer(playerId);
        return player != null ? plugin.getEconomy().getBalance(player) : 0.0;
    }

    public double getBankMoney(UUID playerId) {
        return bankMoney.getOrDefault(playerId, plugin.getConfigManager().getInitialBank());
    }

    public void addPlayerMoney(UUID playerId, double amount) {
        if (!plugin.isVaultEnabled()) {
            playerMoney.merge(playerId, amount, Double::sum);
            return;
        }
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            plugin.getEconomy().depositPlayer(player, amount);
        }
    }

    public void addBankMoney(UUID playerId, double amount) {
        bankMoney.merge(playerId, amount, Double::sum);
    }

    public boolean removePlayerMoney(UUID playerId, double amount) {
        if (!plugin.isVaultEnabled()) {
            double current = getPlayerMoney(playerId);
            if (current >= amount) {
                playerMoney.put(playerId, current - amount);
                return true;
            }
            return false;
        }
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && plugin.getEconomy().has(player, amount)) {
            plugin.getEconomy().withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public boolean removeBankMoney(UUID playerId, double amount) {
        double current = getBankMoney(playerId);
        if (current >= amount) {
            bankMoney.put(playerId, current - amount);
            return true;
        }
        return false;
    }

    public void handlePlayerDeath(Player player) {
        UUID playerId = player.getUniqueId();
        double current = getPlayerMoney(playerId);
        double penalty = plugin.getConfigManager().getDeathPenalty();
        double loss = current * penalty;

        if (loss > 0) {
            removePlayerMoney(playerId, loss);
            player.sendMessage("§cStraciłeś " + String.format("%.2f", loss) + "$ podczas śmierci!");
        }
    }

    public List<Map.Entry<UUID, Double>> getMoneyTopList(int limit) {
        List<Map.Entry<UUID, Double>> topList = new ArrayList<>();
        if (!plugin.isVaultEnabled()) {
            topList.addAll(playerMoney.entrySet());
        } else {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                topList.add(new AbstractMap.SimpleEntry<>(player.getUniqueId(), plugin.getEconomy().getBalance(player)));
            }
        }
        return topList.stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    public List<Map.Entry<UUID, Double>> getBankTopList(int limit) {
        return bankMoney.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    public void loadPlayerData(UUID playerId) {
        if (!plugin.isVaultEnabled()) {
            if (!playerMoney.containsKey(playerId)) {
                playerMoney.put(playerId, plugin.getConfigManager().getInitialMoney());
            }
        }
        if (!bankMoney.containsKey(playerId)) {
            bankMoney.put(playerId, plugin.getConfigManager().getInitialBank());
        }
    }

    public Map<UUID, Double> getPlayerMoneyMap() {
        return new HashMap<>(playerMoney);
    }

    public Map<UUID, Double> getBankMoneyMap() {
        return new HashMap<>(bankMoney);
    }

    public void loadFromMap(Map<String, Map<UUID, Double>> data) {
        if (!plugin.isVaultEnabled()) {
            playerMoney.clear();
            if (data.containsKey("playerMoney")) {
                Map<UUID, Double> playerMoneyData = data.get("playerMoney");
                playerMoney.putAll(playerMoneyData);
            }
        }

        bankMoney.clear();
        if (data.containsKey("bankMoney")) {
            Map<UUID, Double> bankMoneyData = data.get("bankMoney");
            bankMoney.putAll(bankMoneyData);
        }
    }
}