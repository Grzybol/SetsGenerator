package org.betterbox.setsGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private File configFile = null;
    List<String> logLevels = null;

    Set<PluginLogger.LogLevel> enabledLogLevels;
    private final SetsGenerator setsGenerator;
    String folderPath;

    public ConfigManager(JavaPlugin plugin, PluginLogger pluginLogger, String folderPath, SetsGenerator setsGenerator) {
        this.folderPath=folderPath;
        this.plugin = plugin;
        this.setsGenerator=setsGenerator;
        this.pluginLogger = pluginLogger;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: calling configureLogger");
        configureLogger();

    }
    private void CreateExampleConfigFile(String folderPath){
        File exampleConfigFile = new File(folderPath, "config.yml");
        try (InputStream in = plugin.getResource("exampleFiles/config.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Resource 'exampleFiles/config.yml not found.");
                return;
            }
            Files.copy(in, exampleConfigFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml to " + exampleConfigFile + ": " + e.getMessage());
        }
    }

    private void configureLogger() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: configureLogger called");
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            CreateExampleConfigFile(folderPath);
        }
        ReloadConfig();
    }
    public void ReloadConfig(){
        //String folderPath = plugin.getDataFolder().getAbsolutePath();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig called");
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.reloadConfig();
        logLevels = plugin.getConfig().getStringList("log_level");
        enabledLogLevels = new HashSet<>();
        if (logLevels == null || logLevels.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"ConfigManager: ReloadConfig: no config file or no configured log levels! Saving default settings.");
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");

        }
        for (String level : logLevels) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig: adding "+level.toUpperCase());
                enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig: current log levels: "+ Arrays.toString(enabledLogLevels.toArray()));

            } catch (IllegalArgumentException e) {
                // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
            }
        }
        // Ładowanie wartości kolorów startowego i końcowego
        setsGenerator.setStartColor(getConfigString("startColor", "255,255,255")); // Domyślny biały kolor
        setsGenerator.setEndColor(getConfigString("endColor", "0,0,0")); // Domyślny czarny kolor

        //REQUIRED ITEMS SECTION

        setsGenerator.setChestplateBaseProtection(getConfigInt("chestplateBaseProtection", 5));
        setsGenerator.setLeggingsBaseProtection(getConfigInt("leggingsBaseProtection", 4));
        setsGenerator.setHelmetBaseProtection(getConfigInt("helmetBaseProtection", 3));
        setsGenerator.setBootsBaseProtection(getConfigInt("bootsBaseProtection", 2));
        setsGenerator.setChestplateProtectionPerLvL(getConfigInt("chestplateProtectionPerLvL", 1));
        setsGenerator.setLeggingsProtectionPerLvL(getConfigInt("leggingsProtectionPerLvL", 1));
        setsGenerator.setBootsProtectionPerLvL(getConfigInt("bootsProtectionPerLvL", 1));
        setsGenerator.setHelmetProtectionPerLvL(getConfigInt("helmetProtectionPerLvL", 1));
        setsGenerator.setSwordDamage(getConfigInt("swordDamage", 7));
        setsGenerator.setTalismanMovementSpeedBonus(getConfigDouble("talismanMovementSpeedBonus", 0.05));
        setsGenerator.setTalismanHealthBonus(getConfigInt("talismanHealthBonus", 20));
        setsGenerator.setChestplateHealthBonus(getConfigInt("chestplateHealthBonus", 10));
        setsGenerator.setLeggingHealthBonus(getConfigInt("leggingHealthBonus", 8));
        setsGenerator.setBootsHealthBonus(getConfigInt("bootsHealthBonus", 6));
        setsGenerator.setHelmetHealthBonus(getConfigInt("helmetHealthBonus", 4));


        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager.ReloadConfig calling pluginLogger.setEnabledLogLevels(enabledLogLevels) with parameters: "+ Arrays.toString(enabledLogLevels.toArray()));
        // Ustawienie aktywnych poziomów logowania w loggerze
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
        loadUpgradeItems();
    }
    private String getConfigString(String path, String defaultValue) {
        if (plugin.getConfig().contains(path)) {
            if (plugin.getConfig().isString(path)) {
                return plugin.getConfig().getString(path);
            } else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config value for " + path + " is not a string, defaulting to " + defaultValue);
                plugin.getConfig().set(path, defaultValue);
                plugin.saveConfig();
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config section " + path + " not found, creating with default " + defaultValue);
            plugin.getConfig().set(path, defaultValue);
            plugin.saveConfig();
        }
        return defaultValue;
    }
    private int getConfigInt(String path, int defaultValue) {
        if (plugin.getConfig().contains(path)) {
            if (plugin.getConfig().isInt(path)) {
                return plugin.getConfig().getInt(path);
            } else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config value for " + path + " is not an int, defaulting to " + defaultValue);
                plugin.getConfig().set(path, defaultValue);
                plugin.saveConfig();
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config section " + path + " not found, creating with default " + defaultValue);
            plugin.getConfig().set(path, defaultValue);
            plugin.saveConfig();
        }
        return defaultValue;
    }

    private double getConfigDouble(String path, double defaultValue) {
        if (plugin.getConfig().contains(path)) {
            if (plugin.getConfig().isDouble(path)) {
                return plugin.getConfig().getDouble(path);
            } else {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config value for " + path + " is not a double, defaulting to " + defaultValue);
                plugin.getConfig().set(path, defaultValue);
                plugin.saveConfig();
            }
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config section " + path + " not found, creating with default " + defaultValue);
            plugin.getConfig().set(path, defaultValue);
            plugin.saveConfig();
        }
        return defaultValue;
    }
    public ItemStack loadItemStackFromFile(String relativeFilePath) {
        try {
            File file = new File(plugin.getDataFolder(), relativeFilePath);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading ItemStack from file: " + file.getAbsolutePath()+", provided relativeFilePath: "+relativeFilePath);

            if (!file.exists()) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, error: file doesn't exist. filePath: " + file.getAbsolutePath());
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ItemStack itemStack = config.getItemStack("item");
            if (itemStack == null) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, error: itemStack=null. filePath: " + file.getAbsolutePath());
                return null;
            }
            return itemStack;
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "loadItemStackFromFile, filePath: " + relativeFilePath + ", error: " + e.getMessage());
            return null;
        }
    }


    public void addItemStack(String materialName, int amount) {
        // Sprawdzenie czy podany materiał istnieje w enumie Material
        Material material = Material.matchMaterial(materialName);
        if (material != null) {
            // Tworzenie nowego ItemStack z określonym materiałem i ilością
            ItemStack itemStack = new ItemStack(material, amount);
            // Dodanie ItemStack do listy
            //requiredItemStacks.add(itemStack);
        } else {
            // Obsługa sytuacji, gdy nazwa materiału jest nieprawidłowa
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"addItemStack, no material found for materialName: "+materialName);
        }
    }
    public void updateConfig(String configuration) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: updateConfig called with parameters "+ configuration);
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            lines.add(configuration);
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
    private void loadUpgradeItems() {
        plugin.reloadConfig();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("upgradeItemsSection");
        if (section == null) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Config section 'upgradeItemsSection' not found.");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                Map<ItemStack, Integer> items = new HashMap<>();
                List<String> itemList = section.getStringList(key);

                for (String item : itemList) {
                    String[] parts = item.split(" ");
                    if (parts.length == 3) {
                        String itemIdentifier = parts[0];
                        int amount = Integer.parseInt(parts[1]);

                        ItemStack itemStack = null;
                        if (itemIdentifier.endsWith(".yml")) {
                            // Wczytywanie niestandardowego przedmiotu z pliku
                            itemStack = setsGenerator.getFileManager().loadItemStackFromFile(itemIdentifier);
                        } else {
                            // Wczytywanie standardowego przedmiotu Minecraft
                            Material material = Material.matchMaterial(itemIdentifier);
                            if (material != null) {
                                itemStack = new ItemStack(material, amount);
                            }
                        }

                        if (itemStack != null) {
                            items.put(itemStack, amount);
                        } else {
                            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid material or file for: " + itemIdentifier);
                        }
                    } else {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid item format: " + item);
                    }
                }

                setsGenerator.getUpgradeLists().put(id, items);
            } catch (NumberFormatException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid key format in 'upgradeItemsSection': " + key);
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.INFO, "Upgrade items loaded successfully.");
    }
}
