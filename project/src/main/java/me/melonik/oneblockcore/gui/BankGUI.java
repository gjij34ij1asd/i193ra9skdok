package me.melonik.oneblockcore.gui;

import me.melonik.oneblockcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BankGUI implements Listener {
    private final Main plugin;
    private final Map<UUID, Boolean> awaitingDeposit = new HashMap<>();
    private final Map<UUID, Long> inputTimeout = new HashMap<>();

    public BankGUI(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Bankier");

        // Stan konta
        double balance = plugin.getEconomyManager().getBankMoney(player.getUniqueId());
        ItemStack balanceItem = createItem(Material.PAPER, "§3Stan konta",
                "§7Posiadasz: §a" + String.format("%.2f$", balance));
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        balanceMeta.setCustomModelData(9993);
        balanceItem.setItemMeta(balanceMeta);
        inv.setItem(11, balanceItem);

        // Zamknij
        ItemStack closeItem = createItem(Material.PAPER, "§cZamknij!", "§7Kliknij, aby zamknąć gui");
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setCustomModelData(9994);
        closeItem.setItemMeta(closeMeta);
        inv.setItem(13, closeItem);

        // Wpłata/Wypłata
        ItemStack moneyItem = createItem(Material.PAPER, "§3Operacje pieniężne",
                "§x§E§C§9§6§0§0Kliknij LEWYM aby wpłacić pieniądze",
                "§x§0§9§D§7§2§2Kliknij PRAWYM aby wypłacić pieniądze");
        ItemMeta moneyMeta = moneyItem.getItemMeta();
        moneyMeta.setCustomModelData(9992);
        moneyItem.setItemMeta(moneyMeta);
        inv.setItem(15, moneyItem);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Bankier")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (event.getSlot() == 13) {
            player.closeInventory();
        } else if (event.getSlot() == 15) {
            boolean isDeposit = event.isLeftClick();
            startMoneyInput(player, isDeposit);
        }
    }

    private void startMoneyInput(Player player, boolean isDeposit) {
        player.closeInventory();
        awaitingDeposit.put(player.getUniqueId(), isDeposit);
        inputTimeout.put(player.getUniqueId(), System.currentTimeMillis());

        String action = isDeposit ? "wpłacić" : "wypłacić";
        player.sendTitle("§2§lBankier", "§aWprowadź na czacie ilość pięniedzy jaką chcesz " + action, 10, 150, 10);

        // Anuluj input po 15 sekundach
        new BukkitRunnable() {
            @Override
            public void run() {
                if (awaitingDeposit.containsKey(player.getUniqueId())) {
                    awaitingDeposit.remove(player.getUniqueId());
                    inputTimeout.remove(player.getUniqueId());
                    player.sendTitle("§4§lBłąd!","§cCzas na wpisanie kwoty minął!",10,40,20);
                }
            }
        }.runTaskLater(plugin, 300L); // 15 sekund
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingDeposit.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        String input = event.getMessage();
        boolean isDeposit = awaitingDeposit.get(player.getUniqueId());
        awaitingDeposit.remove(player.getUniqueId());
        inputTimeout.remove(player.getUniqueId());

        double amount = parseAmount(input);
        if (amount <= 0) {
            player.sendTitle("§4§lBłąd!","§cNieprawidłowa kwota!",10,40,20);
            return;
        }

        if (isDeposit) {
            handleDeposit(player, amount);
        } else {
            handleWithdraw(player, amount);
        }
    }

    private double parseAmount(String input) {
        try {
            input = input.toLowerCase();
            double multiplier = 1;

            if (input.endsWith("k")) {
                multiplier = 1_000;
                input = input.substring(0, input.length() - 1);
            } else if (input.endsWith("mln")) {
                multiplier = 1_000_000;
                input = input.substring(0, input.length() - 1);
            } else if (input.endsWith("mld")) {
                multiplier = 1_000_000_000;
                input = input.substring(0, input.length() - 1);
            }

            return Double.parseDouble(input) * multiplier;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleDeposit(Player player, double amount) {
        if (!plugin.getEconomyManager().removePlayerMoney(player.getUniqueId(), amount)) {
            player.sendTitle("§4§lBłąd!","§cNie posiadas tyle pięniedzy!",10,40,20);
            return;
        }

        plugin.getEconomyManager().addBankMoney(player.getUniqueId(), amount);
        player.sendTitle("§2§lSuckes!","§aWpłacono §f" + String.format("%.2f$", amount) + " §ado banku!",10,40,20);
    }

    private void handleWithdraw(Player player, double amount) {
        if (plugin.getEconomyManager().getBankMoney(player.getUniqueId()) < amount) {
            player.sendTitle("§4§lBłąd!","§cNie posiadasz tyle pieniędzy w banku!",10,40,20);
            return;
        }

        plugin.getEconomyManager().removeBankMoney(player.getUniqueId(), amount);
        plugin.getEconomyManager().addPlayerMoney(player.getUniqueId(), amount);
        player.sendTitle("§2§lSuckes!","§aWypłacono §f" + String.format("%.2f$", amount) + " §az banku!",10,40,20);
    }
}