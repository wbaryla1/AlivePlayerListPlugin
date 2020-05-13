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
        removeDuplicates();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("alivelist")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "You cannot run this command");
                return true;
            }

            Player player = (Player)sender;

            if (args.length == 0) {
                player.sendMessage(convertList());
                return true;
            }
            else {
                Team team = this.getServer().getScoreboardManager().getMainScoreboard().getTeam(args[0]);
                List<String> players = this.getConfig().getStringList("players." + team.getName());
                List<String> names = new ArrayList<String>();
                for (String s : players) {
                    names.add(team.getColor() + s);
                }

                if (names.size() > 0)
                    player.sendMessage(String.join(", ", names));
                return true;
            }
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
                    getConfig().set("players." + team.getName(), list);
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

        removeDuplicates();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Team t = p.getScoreboard().getPlayerTeam(p);

        removeDuplicates();

        if (getConfig().getStringList("players." + t.getName()).contains(p.getName())) {
            List<String> list = getConfig().getStringList("players." + t.getName());
            list.remove(p.getName());
            getConfig().set("players." + t.getName(), list);
            saveConfig();
        }

        removeDuplicates();
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

    public void removeDuplicates() {
        // needed for when someone gets promoted, that they aren't recorded under both Acolyte + Brother
        Set<Team> teams = this.getServer().getScoreboardManager().getMainScoreboard().getTeams();
        Set<String> names = new HashSet<String>();

        for (Team t : teams) {
            for (String s : getConfig().getStringList("players." + t.getName())) {
                if (!names.contains(s)) {
                    names.add(s);
                }
            }
        }

        getConfig().set("players", "");
        List<String> newList = new ArrayList<String>();

        for (String s : names) {
            for (Team t : teams) {
                if (t.getEntries().contains(s)){
                    List<String> omegaList = getConfig().getStringList("players." + t.getName());
                    omegaList.add(s);
                    getConfig().set("players." + t.getName(), omegaList);
                    saveConfig();
                }
            }
        }
    }
}
