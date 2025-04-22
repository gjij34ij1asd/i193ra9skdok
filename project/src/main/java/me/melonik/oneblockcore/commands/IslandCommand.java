package me.melonik.oneblockcore.commands;

import me.melonik.oneblockcore.Main;
import me.melonik.oneblockcore.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class IslandCommand implements CommandExecutor {
    private final Main plugin;

    public IslandCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§8» §cTa komenda jest dostępna tylko dla graczy!");
            return true;
        }

        if (args.length == 0) {
            Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
            if (island == null) {
                island = plugin.getIslandManager().createIsland(player);
                if (island != null) {
                    Location teleportLoc = island.getCenter().clone().add(0.5, 1, 0.5);
                    player.teleport(teleportLoc);
                    player.sendMessage("§8» §aUtworzono nową wyspę!");
                }
            } else {
                Location teleportLocation = island.getSpawnLocation();
                if (teleportLocation == null) {
                    teleportLocation = island.getCenter().clone().add(0.5, 1, 0.5);
                }
                player.teleport(teleportLocation);
                player.sendMessage("§aTeleportowano na wyspę!");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "usun":
                deleteIsland(player);
                break;
            case "zapros":
                if (args.length < 2) {
                    player.sendMessage("§8» §3Przydatne komendy OneBlock");
                    player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
                    player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
                    player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
                    player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
                    player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
                    player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
                    player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
                    player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
                    player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
                    return true;
                }
                invitePlayer(player, args[1]);
                break;
            case "akceptuj":
                acceptInvite(player);
                break;
            case "odrzuc":
                rejectInvite(player);
                break;
            case "wyrzuc":
                if (args.length < 2) {
                    player.sendMessage("§8» §3Przydatne komendy OneBlock");
                    player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
                    player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
                    player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
                    player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
                    player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
                    player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
                    player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
                    player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
                    player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
                    return true;
                }
                kickPlayer(player, args[1]);
                break;
            case "opusc":
                leaveIsland(player);
                break;
            case "ban":
                if (args.length < 2) {
                    player.sendMessage("§8» §3Przydatne komendy OneBlock");
                    player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
                    player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
                    player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
                    player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
                    player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
                    player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
                    player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
                    player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
                    player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
                    return true;
                }
                banPlayer(player, args[1]);
                break;
            case "unban":
                if (args.length < 2) {
                    player.sendMessage("§8» §3Przydatne komendy OneBlock");
                    player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
                    player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
                    player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
                    player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
                    player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
                    player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
                    player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
                    player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
                    player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
                    return true;
                }
                unbanPlayer(player, args[1]);
                break;
            case "ustawwyspa":
                setSpawnPoint(player);
                break;
            case "odwiedz":
                if (args.length < 2) {
                    player.sendMessage("§8» §3Przydatne komendy OneBlock");
                    player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
                    player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
                    player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
                    player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
                    player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
                    player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
                    player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
                    player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
                    player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
                    player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
                    return true;
                }
                visitIsland(player, args[1]);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8» §3Przydatne komendy OneBlock");
        player.sendMessage("§8» §b/is §7- Teleportuje na wyspę lub tworzy nową");
        player.sendMessage("§8» §b/is usun §7- Usuwa twoją wyspę");
        player.sendMessage("§8» §b/is zapros <gracz> §7- Zaprasza gracza na wyspę");
        player.sendMessage("§8» §b/is akceptuj §7- Akceptuje zaproszenie na wyspę");
        player.sendMessage("§8» §b/is odrzuc §7- Odrzuca zaproszenie na wyspę");
        player.sendMessage("§8» §b/is wyrzuc <gracz> §7- Wyrzuca gracza z wyspy");
        player.sendMessage("§8» §b/is opusc §7- Opuszcza wyspę");
        player.sendMessage("§8» §b/is ban <gracz> §7- Banuje gracza na wyspie");
        player.sendMessage("§8» §b/is unban <gracz> §7- Odbanowuje gracza na wyspie");
        player.sendMessage("§8» §b/is ustawwyspa §7- Ustawia punkt odrodzenia na wyspie");
        player.sendMessage("§8» §b/is odwiedz <gracz> §7- Odwiedza wyspę gracza");
    }

    private void deleteIsland(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        plugin.getIslandManager().deleteIsland(island.getIslandId());
    }

    private void invitePlayer(Player player, String targetName) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§8» §cGracz nie jest online!");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie możesz zaprosić samego siebie!");
            return;
        }

        if (island.getMembers().contains(target.getUniqueId())) {
            player.sendMessage("§8» §cTen gracz jest już członkiem twojej wyspy!");
            return;
        }

        if (island.getBanned().contains(target.getUniqueId())) {
            player.sendMessage("§8» §cTen gracz jest zbanowany na twojej wyspie!");
            return;
        }

        if (plugin.getIslandManager().hasIsland(target.getUniqueId())) {
            player.sendMessage("§8» §cTen gracz posiada już wyspę!");
            return;
        }

        if (island.getMembers().size() >= plugin.getConfigManager().getMaxMembers()) {
            player.sendMessage("§8» §cOsiągnięto maksymalną liczbę członków!");
            return;
        }

        plugin.getIslandManager().invitePlayer(island.getIslandId(), target.getUniqueId());
        player.sendMessage("§8» §aWysłano zaproszenie do " + target.getName());
        target.sendMessage("§8» §aOtrzymałeś zaproszenie na wyspę od " + player.getName());
        target.sendMessage("§8» §aUżyj §e/is akceptuj §aaby dołączyć lub §e/is odrzuc §aaby odrzucić");
    }

    private void acceptInvite(Player player) {
        UUID invitationIslandId = plugin.getIslandManager().getInvitation(player.getUniqueId());
        if (invitationIslandId == null) {
            player.sendMessage("§8» §cNie masz żadnego aktywnego zaproszenia!");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(invitationIslandId);
        if (island == null) {
            player.sendMessage("§8» §cWyspa nie istnieje!");
            plugin.getIslandManager().removeInvitation(player.getUniqueId());
            return;
        }

        plugin.getIslandManager().addMember(invitationIslandId, player.getUniqueId());
        player.sendMessage("§8» §aDołączyłeś do wyspy!");

        Player owner = Bukkit.getPlayer(island.getOwnerId());
        if (owner != null) {
            owner.sendMessage("§8» §a" + player.getName() + " dołączył do twojej wyspy!");
        }

        Location teleportLocation = island.getSpawnLocation();
        if (teleportLocation == null) {
            teleportLocation = island.getCenter().clone().add(0.5, 1, 0.5);
        }
        player.teleport(teleportLocation);
    }

    private void rejectInvite(Player player) {
        if (plugin.getIslandManager().getInvitation(player.getUniqueId()) == null) {
            player.sendMessage("§8» §cNie masz żadnego aktywnego zaproszenia!");
            return;
        }

        plugin.getIslandManager().removeInvitation(player.getUniqueId());
        player.sendMessage("§8» §cOdrzuciłeś zaproszenie na wyspę");
    }

    private void leaveIsland(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie należysz do żadnej wyspy!");
            return;
        }

        if (island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cJesteś właścicielem wyspy! Użyj §e/is usun §caby usunąć wyspę.");
            return;
        }

        plugin.getIslandManager().removeMember(island.getIslandId(), player.getUniqueId());
        player.sendMessage("§8» §cOpuściłeś wyspę!");

        Player owner = Bukkit.getPlayer(island.getOwnerId());
        if (owner != null) {
        }

        World world = Bukkit.getWorld("world");
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        }
    }

    private void kickPlayer(Player player, String targetName) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        Player targetOnline = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String targetDisplayName;

        if (targetOnline != null) {
            targetUUID = targetOnline.getUniqueId();
            targetDisplayName = targetOnline.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
            if (targetOffline.hasPlayedBefore()) {
                targetUUID = targetOffline.getUniqueId();
                targetDisplayName = targetOffline.getName();
            } else {
                player.sendMessage("§8» §cNie znaleziono takiego gracza!");
                return;
            }
        }

        if (!island.getMembers().contains(targetUUID)) {
            player.sendMessage("§8» §cTen gracz nie jest członkiem twojej wyspy!");
            return;
        }

        plugin.getIslandManager().removeMember(island.getIslandId(), targetUUID);
        player.sendMessage("§8» §aWyrzucono gracza " + targetDisplayName + " z wyspy!");

        if (targetOnline != null) {
            targetOnline.sendMessage("§8» §cZostałeś wyrzucony z wyspy!");
            World world = Bukkit.getWorld("world");
            if (world != null) {
                targetOnline.teleport(world.getSpawnLocation());
            }
        }
    }

    private void banPlayer(Player player, String targetName) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        Player targetOnline = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String targetDisplayName;

        if (targetOnline != null) {
            targetUUID = targetOnline.getUniqueId();
            targetDisplayName = targetOnline.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
            if (targetOffline.hasPlayedBefore()) {
                targetUUID = targetOffline.getUniqueId();
                targetDisplayName = targetOffline.getName();
            } else {
                player.sendMessage("§8» §cNie znaleziono takiego gracza!");
                return;
            }
        }

        if (targetUUID.equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie możesz zbanować samego siebie!");
            return;
        }

        if (island.getBanned().contains(targetUUID)) {
            player.sendMessage("§8» §cTen gracz jest już zbanowany!");
            return;
        }

        if (island.getMembers().contains(targetUUID)) {
            plugin.getIslandManager().removeMember(island.getIslandId(), targetUUID);
        }

        plugin.getIslandManager().banPlayer(island.getIslandId(), targetUUID);
        player.sendMessage("§8» §aZbanowano gracza " + targetDisplayName);

        if (targetOnline != null) {
            targetOnline.sendMessage("§8» §cZostałeś zbanowany na wyspie gracza " + player.getName());
            if (island.isOnIsland(targetOnline)) {
                World world = Bukkit.getWorld("world");
                if (world != null) {
                    targetOnline.teleport(world.getSpawnLocation());
                }
            }
        }
    }

    private void unbanPlayer(Player player, String targetName) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        Player targetOnline = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String targetDisplayName;

        if (targetOnline != null) {
            targetUUID = targetOnline.getUniqueId();
            targetDisplayName = targetOnline.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
            if (targetOffline.hasPlayedBefore()) {
                targetUUID = targetOffline.getUniqueId();
                targetDisplayName = targetOffline.getName();
            } else {
                player.sendMessage("§8» §cNie znaleziono takiego gracza!");
                return;
            }
        }

        if (!island.getBanned().contains(targetUUID)) {
            player.sendMessage("§8» §cTen gracz nie jest zbanowany!");
            return;
        }

        plugin.getIslandManager().unbanPlayer(island.getIslandId(), targetUUID);
        player.sendMessage("§8» §aOdbanowano gracza " + targetDisplayName);

        if (targetOnline != null) {
            targetOnline.sendMessage("§8» §aZostałeś odbanowany na wyspie gracza " + player.getName());
        }
    }

    private void setSpawnPoint(Player player) {
        Island island = plugin.getIslandManager().getPlayerIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage("§8» §cNie posiadasz wyspy!");
            return;
        }

        if (!island.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§8» §cNie jesteś właścicielem tej wyspy!");
            return;
        }

        Location location = player.getLocation();
        if (!island.isOnIsland(player)) {
            player.sendMessage("§8» §cMusisz być na swojej wyspie aby ustawić miejsce teleportu!");
            return;
        }

        island.setSpawnLocation(location);
        player.sendMessage("§8» §aUstawiono miejsce teleportu na wyspe!");
    }

    private void visitIsland(Player player, String targetName) {
        Player targetOnline = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String targetDisplayName;

        if (targetOnline != null) {
            targetUUID = targetOnline.getUniqueId();
            targetDisplayName = targetOnline.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
            if (targetOffline.hasPlayedBefore()) {
                targetUUID = targetOffline.getUniqueId();
                targetDisplayName = targetOffline.getName();
            } else {
                player.sendMessage("§8» §cNie znaleziono takiego gracza!");
                return;
            }
        }

        Island targetIsland = plugin.getIslandManager().getPlayerIsland(targetUUID);
        if (targetIsland == null) {
            player.sendMessage("§8» §cTen gracz nie posiada wyspy!");
            return;
        }

        if (!targetIsland.isVisitable()) {
            player.sendMessage("§8» §cTa wyspa nie przyjmuje odwiedzających!");
            return;
        }

        if (targetIsland.getBanned().contains(player.getUniqueId())) {
            player.sendMessage("§8» §cJesteś zbanowany na tej wyspie!");
            return;
        }

        Location teleportLocation = targetIsland.getSpawnLocation();
        if (teleportLocation == null) {
            teleportLocation = targetIsland.getCenter().clone().add(0.5, 1, 0.5);
        }

        player.teleport(teleportLocation);
        player.sendMessage("§8» §aTeleportowano na wyspę gracza " + targetDisplayName);
    }
}