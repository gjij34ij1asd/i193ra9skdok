package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoCommand implements CommandExecutor {
    private final Main plugin;

    public EcoCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("oneblock.eco")) {
            sender.sendMessage("§cNie masz uprawnień!");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage("§cUżyj: /eco <give|take|set> <gracz> <kwota>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cGracz nie jest online!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cNieprawidłowa kwota!");
            return true;
        }

        if (amount < 0) {
            sender.sendMessage("§cKwota nie może być ujemna!");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                plugin.getEconomy().depositPlayer(target, amount);
                sender.sendMessage("§aDodano §6$" + String.format("%.2f", amount) + " §ado konta gracza §e" + target.getName());
                target.sendMessage("§aOtrzymano §6$" + String.format("%.2f", amount) + " §aod administratora");
                break;
            case "take":
                if (plugin.getEconomy().has(target, amount)) {
                    plugin.getEconomy().withdrawPlayer(target, amount);
                    sender.sendMessage("§aZabrano §6$" + String.format("%.2f", amount) + " §az konta gracza §e" + target.getName());
                    target.sendMessage("§cZabrano §6$" + String.format("%.2f", amount) + " §cz twojego konta");
                } else {
                    sender.sendMessage("§cGracz nie ma tylu pieniędzy!");
                }
                break;
            case "set":
                double current = plugin.getEconomy().getBalance(target);
                if (current > amount) {
                    plugin.getEconomy().withdrawPlayer(target, current - amount);
                } else if (current < amount) {
                    plugin.getEconomy().depositPlayer(target, amount - current);
                }
                sender.sendMessage("§aUstawiono stan konta gracza §e" + target.getName() + " §ana §6$" + String.format("%.2f", amount));
                target.sendMessage("§aTwój stan konta został ustawiony na §6$" + String.format("%.2f", amount));
                break;
            default:
                sender.sendMessage("§cNieznana operacja! Użyj: give, take lub set");
                break;
        }

        return true;
    }
}