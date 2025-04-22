package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminCommand implements CommandExecutor {
    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("oneblock.admin")) {
            sender.sendMessage("§cNie masz uprawnień do tej komendy!");
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "delete":
                deleteIsland(sender, args[1]);
                break;
            case "kick":
                kickPlayer(sender, args[1]);
                break;
            case "setowner":
                if (args.length < 3) {
                    sender.sendMessage("§8» §3Komendy admina na oneblock");
                    sender.sendMessage("§8» §b/oa delete <gracz> §7- Usuwa wyspę gracza");
                    sender.sendMessage("§8» §b/oa kick <gracz> §7- Wyrzuca gracza z wyspy");
                    sender.sendMessage("§8» §b/oa setowner <gracz> <nowy właściciel> §7- Zmienia właściciela wyspy");
                    sender.sendMessage("§8» §b/oa addmoney <gracz> <ilość> §7- Dodaje pieniądze graczowi");
                    return true;
                }
                setOwner(sender, args[1], args[2]);
                break;
            case "addmoney":
                if (args.length < 3) {
                    sender.sendMessage("§8» §3Komendy admina na oneblock");
                    sender.sendMessage("§8» §b/oa delete <gracz> §7- Usuwa wyspę gracza");
                    sender.sendMessage("§8» §b/oa kick <gracz> §7- Wyrzuca gracza z wyspy");
                    sender.sendMessage("§8» §b/oa setowner <gracz> <nowy właściciel> §7- Zmienia właściciela wyspy");
                    sender.sendMessage("§8» §b/oa addmoney <gracz> <ilość> §7- Dodaje pieniądze graczowi");
                    return true;
                }
                addMoney(sender, args[1], args[2]);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8» §3Komendy admina na OneBlock");
        sender.sendMessage("§8» §b/oa delete <gracz> §7- Usuwa wyspę gracza");
        sender.sendMessage("§8» §b/oa kick <gracz> §7- Wyrzuca gracza z wyspy");
        sender.sendMessage("§8» §b/oa setowner <gracz> <nowy właściciel> §7- Zmienia właściciela wyspy");
        sender.sendMessage("§8» §b/oa addmoney <gracz> <ilość> §7- Dodaje pieniądze graczowi");
    }

    private void deleteIsland(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§8» §cGracz nie jest online!");
            return;
        }

        Island island = plugin.getIslandManager().getPlayerIsland(target.getUniqueId());
        if (island == null) {
            sender.sendMessage("§8» §cTen gracz nie posiada wyspy!");
            return;
        }

        plugin.getIslandManager().deleteIsland(island.getIslandId());
        sender.sendMessage("§8» §aUsunięto wyspę gracza " + target.getName());
        target.sendMessage("§8» §cTwoja wyspa została usunięta przez administratora!");
    }

    private void kickPlayer(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§8» §cGracz nie jest online!");
            return;
        }

        Island island = plugin.getIslandManager().getPlayerIsland(target.getUniqueId());
        if (island == null) {
            sender.sendMessage("§8» §cTen gracz nie należy do żadnej wyspy!");
            return;
        }

        if (island.getOwnerId().equals(target.getUniqueId())) {
            sender.sendMessage("§8» §cNie możesz wyrzucić właściciela wyspy!");
            return;
        }

        plugin.getIslandManager().removeMember(island.getIslandId(), target.getUniqueId());
        sender.sendMessage("§8» §aWyrzucono gracza " + target.getName() + " z wyspy!");
        target.sendMessage("§8» §cZostałeś wyrzucony z wyspy przez administratora!");
        target.teleport(target.getWorld().getSpawnLocation());
    }

    private void setOwner(CommandSender sender, String currentOwnerName, String newOwnerName) {
        Player currentOwner = Bukkit.getPlayer(currentOwnerName);
        Player newOwner = Bukkit.getPlayer(newOwnerName);

        if (currentOwner == null || newOwner == null) {
            sender.sendMessage("§8» §cJeden z graczy nie jest online!");
            return;
        }

        Island island = plugin.getIslandManager().getPlayerIsland(currentOwner.getUniqueId());
        if (island == null) {
            sender.sendMessage("§8» §cTen gracz nie posiada wyspy!");
            return;
        }

        UUID oldOwnerId = island.getOwnerId();
        island.getMembers().remove(oldOwnerId);
        island.getMembers().add(newOwner.getUniqueId());
        island.setOwnerId(newOwner.getUniqueId());

        plugin.getIslandManager().saveIslands();

        sender.sendMessage("§8» §aZmieniono właściciela wyspy z " + currentOwner.getName() + " na " + newOwner.getName());
        currentOwner.sendMessage("§8» §cNie jesteś już właścicielem wyspy!");
        newOwner.sendMessage("§8» §aZostałeś nowym właścicielem wyspy!");
    }

    private void addMoney(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§8» §cGracz nie jest online!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§8» §cKwota musi być większa od 0!");
                return;
            }

            plugin.getEconomyManager().addPlayerMoney(target.getUniqueId(), amount);
            sender.sendMessage("§8» §aDodano §6$" + amount + " §adla gracza " + target.getName());
            target.sendMessage("§8» §aOtrzymałeś §6$" + amount + " §aod administratora!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§8» §cNieprawidłowa kwota!");
        }
    }
}