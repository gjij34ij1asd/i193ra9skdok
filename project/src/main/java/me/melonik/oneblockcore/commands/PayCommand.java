package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    private final Main plugin;

    public PayCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cTa komenda jest dostępna tylko dla graczy!");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUżyj: /pay <gracz> <kwota>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("§cGracz nie jest online!");
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage("§cNie możesz przelać pieniędzy samemu sobie!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNieprawidłowa kwota!");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cKwota musi być większa od 0!");
            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {
            sender.sendMessage("§cNie masz wystarczająco pieniędzy!");
            return true;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);
        plugin.getEconomy().depositPlayer(target, amount);

        player.sendMessage("§aPrzelano §6$" + String.format("%.2f", amount) + " §ado gracza §e" + target.getName());
        target.sendMessage("§aOtrzymano §6$" + String.format("%.2f", amount) + " §aod gracza §e" + player.getName());

        return true;
    }
}