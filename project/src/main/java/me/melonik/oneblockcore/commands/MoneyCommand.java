package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {
    private final Main plugin;

    public MoneyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cTa komenda jest dostępna tylko dla graczy!");
                return true;
            }
            Player player = (Player) sender;
            double balance = plugin.getEconomy().getBalance(player);
            sender.sendMessage("§7Stan konta: §6$" + String.format("%.2f", balance));
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("oneblock.money.others")) {
                sender.sendMessage("§cNie masz uprawnień!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cGracz nie jest online!");
                return true;
            }

            double balance = plugin.getEconomy().getBalance(target);
            sender.sendMessage("§7Stan konta gracza §e" + target.getName() + "§7: §6$" + String.format("%.2f", balance));
            return true;
        }

        sender.sendMessage("§cUżyj: /money [gracz]");
        return true;
    }
}