package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.gui.IslandPanelGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PanelCommand implements CommandExecutor {
    private final Main plugin;

    public PanelCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTa komenda jest dostępna tylko dla graczy!");
            return true;
        }

        if (!plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            player.sendMessage("§cNie posiadasz wyspy!");
            return true;
        }

        // Otwórz GUI panelu
        new IslandPanelGUI(plugin, player).open();
        return true;
    }
}