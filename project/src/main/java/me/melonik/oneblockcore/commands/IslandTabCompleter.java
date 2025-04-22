package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IslandTabCompleter implements TabCompleter {
    private final Main plugin;
    private final List<String> arguments = Arrays.asList(
            "stworz", "usun", "coop", "zapros", "akceptuj", "odrzuc",
            "ban", "unban", "ustawwyspa", "wyrzuc", "odwiedz", "opusc"
    );

    public IslandTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String arg : arguments) {
                if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(arg);
                }
            }
            return result;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("coop") ||
                    subCommand.equals("zapros") ||
                    subCommand.equals("ban") ||
                    subCommand.equals("unban") ||
                    subCommand.equals("wyrzuc") ||
                    subCommand.equals("odwiedz")) {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        result.add(player.getName());
                    }
                }
                return result;
            }
        }

        return result;
    }
}