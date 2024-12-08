package org.betterbox.setsGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final PluginLogger pluginLogger;
    private final String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
    public CommandManager(JavaPlugin plugin, ConfigManager configManager,PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.pluginLogger=pluginLogger;
        plugin.getCommand("sg").setExecutor(this);
        plugin.getCommand("sg").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String transactionID = UUID.randomUUID().toString();
        if (!command.getName().equalsIgnoreCase("sg")) return false;
        if(!sender.isOp()){
            return false;
        }
        boolean isPlayer = false;
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            isPlayer = true;
        }else{
            isPlayer=false;
        }


        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /sg <reloadconfig|spawnnpc>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                sender.sendMessage(ChatColor.GREEN + "Plugin created by: "+plugin.getDescription().getAuthors());
                sender.sendMessage(ChatColor.GREEN + "version: "+plugin.getDescription().getVersion());
                break;
            case "reloadconfig":
                reloadConfig(sender);
                break;

            case "spawnnpc":
                spawnNPC(sender, args);
                break;

            case "saveitem":
                if(isPlayer) {

                    ((SetsGenerator) plugin).getFileManager().saveItemStackToFile(args[1].toLowerCase(), player.getItemInHand());
                }
                break;
            case "upgradeitem":
                String input = args[3]; // np. args[3] = "42"
                int value;
                try {
                    value = Integer.parseInt(input);
                    // Teraz w "value" znajduje się wartość całkowita, np. 42
                } catch (NumberFormatException e) {
                    // Jeśli nie uda się sparsować (np. input nie jest liczbą),
                    // tutaj można obsłużyć błąd, np. wyświetlić komunikat
                    System.out.println("Please provide a level number.");
                    break;
                }
                Player playerToUpgrade = Bukkit.getPlayer(args[1]);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.OnCommand upgradeItem, playerToUpgrade: "+playerToUpgrade);
                assert playerToUpgrade != null;
                ((SetsGenerator) plugin).upgradeItem(playerToUpgrade,getTagFromInput(args[2]),value,transactionID);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: /sg <reloadconfig|spawnnpc|info|upgradeItem>");
                break;
        }

        return true;
    }

    public String getTagFromInput(String input) {
        if (input == null) {
            return null;
        }

        String lowerInput = input.toLowerCase();
        String resultTag;

        switch (lowerInput) {
            case "chest":
            case "elytra":
                resultTag = "chestplate_level";
                break;
            case "helmet":
            case "head":
                resultTag = "helmet_level";
                break;
            case "leggings":
            case "leg":
            case "legs":
                resultTag = "leggings_level";
                break;
            case "boots":
                resultTag = "boots_level";
                break;
            case "talisman":
                resultTag = "talisman_level";
                break;
            case "sword":
                resultTag = "sword_level";
                break;
            default:
                // Gdy nie pasuje żaden z powyższych przypadków
                // możesz zwrócić np. null albo jakiś domyślny tag
                resultTag = null;
                break;
        }

        return resultTag;
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

        NamespacedKey key = new NamespacedKey(plugin, "SetsGeneratorShop");

        Villager villager = (Villager) player.getWorld().spawnEntity(location, EntityType.VILLAGER);


// Ustawienie właściwości NPC
        villager.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Equipment upgrades");
        villager.setCustomNameVisible(true);
        AttributeInstance attribute = villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(0);
        }
        villager.setAI(true);
        villager.setInvulnerable(true); // Uczyń wieśniaka nieśmiertelnym
        villager.setCollidable(false);
        villager.isInvisible();

// Zapisz informację o tym, że to nasz NPC w PersistentDataContainer
        villager.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SetsGenerator Shop");

        sender.sendMessage(ChatColor.GREEN + "NPC Shop spawned successfully.");


        /*
        Villager villager = player.getWorld().spawn(location, Villager.class, npc -> {
            npc.setAI(false);
            npc.setInvulnerable(true);
            npc.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Shop");
            npc.setCustomNameVisible(true);
            npc.setSilent(true);
            npc.setCollidable(false);
            npc.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SetsGenerator Shop");
        });

        sender.sendMessage(ChatColor.GREEN + "NPC Shop spawned successfully.");

         */

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("sg")) return suggestions;

        if (args.length == 1) {
            if ("reloadconfig".startsWith(args[0].toLowerCase())) suggestions.add("reloadconfig");
            if ("spawnnpc".startsWith(args[0].toLowerCase())) suggestions.add("spawnnpc");
            if ("saveitem".startsWith(args[0].toLowerCase())) suggestions.add("saveitem");
            if ("upgradeItem".startsWith(args[0].toLowerCase())) suggestions.add("upgradeItem");
        }

        return suggestions;
    }
}
