package io.github.wbaryla1.aliveplayerlist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.*;

public final class AlivePlayerList extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("alivelist")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command");
                return true;
            }

            Player player = (Player)sender;
            Team team = player.getScoreboard().getPlayerTeam(player);
            System.out.println(args.length);

            List<String> list = new ArrayList<String>();

            player.sendMessage(convertList());
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Team team = p.getScoreboard().getPlayerTeam(p);

        if (team != null) {
            if (p.isDead()) {
                if (getConfig().getStringList("players." + team.getName()).contains(p.getName())) {
                    List<String> list = getConfig().getStringList("players." + team.getName());
                    list.remove(p.getName());
                    getConfig().set("players", list);
                    saveConfig();
                }
            } else {
                if (team != null) {
                    if (!getConfig().getStringList("players." + team.getName()).contains(p.getName())) {
                        List<String> list = getConfig().getStringList("players." + team.getName());
                        list.add(p.getName());
                        getConfig().set("players." + team.getName(), list);
                        saveConfig();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Team t = p.getScoreboard().getPlayerTeam(p);

        if (getConfig().getStringList("players." + t.getName()).contains(p.getName())) {
            List<String> list = getConfig().getStringList("players." + t.getName());
            list.remove(p.getName());
            getConfig().set("players." + t.getName(), list);
            saveConfig();
        }
    }

    public String convertList() {
        List<String> names = new ArrayList<String>();

        Set<Team> teams = this.getServer().getScoreboardManager().getMainScoreboard().getTeams();


        for (Team t : teams) {
            List<String> players = this.getConfig().getStringList("players." + t.getName());
            for (String s : players) {
                names.add(t.getColor() + s);
            }
        }

        return String.join(", ", names);
    }

//    public void removeDuplicates() {
//        Set<Team> teams = this.getServer().getScoreboardManager().getMainScoreboard().getTeams();
//        Set<String> names;
//
//        for (Team t : teams) {
//            for (String s : getConfig().getStringList("players." + t.getName())) {
//                names.add(s);
//            }
//        }
//    }
}
