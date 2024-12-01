package org.betterbox.setsGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public CommandManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        plugin.getCommand("sg").setExecutor(this);
        plugin.getCommand("sg").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isPlayer = false;
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            isPlayer = true;
        }else{
            isPlayer=false;
        }
        if (!command.getName().equalsIgnoreCase("sg")) return false;

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /sg <reloadconfig|spawnnpc>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reloadconfig":
                reloadConfig(sender);
                break;

            case "spawnnpc":
                spawnNPC(sender, args);
                break;

            case "saveitem":
                if(isPlayer) {

                    ((SetsGenerator) plugin).getFileManager().saveItemStackToFile(args[0].toLowerCase(), player.getItemInHand());
                }

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /sg <reloadconfig|spawnnpc>");
                break;
        }

        return true;
    }

    private void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("setsGenerator.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        configManager.ReloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
    }

    private void spawnNPC(CommandSender sender, String[] args) {
        if (!sender.hasPermission("setsGenerator.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

        // Pobieranie lokalizacji gracza
        Location location = player.getLocation();

        // Tworzenie NPC
        Villager villager = (Villager) player.getWorld().spawnEntity(location, EntityType.VILLAGER);

        // Ustawienia NPC
        villager.setAI(false); // NPC nie może się poruszać
        villager.setInvulnerable(true); // NPC nie otrzymuje obrażeń
        villager.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Shop"); // Złota pogrubiona nazwa
        villager.setCustomNameVisible(true);
        villager.setSilent(true);
        villager.setCollidable(false);
        villager.setMetadata("SetsGeneratorShop", new FixedMetadataValue(plugin, "SetsGenerator Shop"));

        sender.sendMessage(ChatColor.GREEN + "NPC Shop spawned successfully.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("sg")) return suggestions;

        if (args.length == 1) {
            if ("reloadconfig".startsWith(args[0].toLowerCase())) suggestions.add("reloadconfig");
            if ("spawnnpc".startsWith(args[0].toLowerCase())) suggestions.add("spawnnpc");
        }

        return suggestions;
    }
}
