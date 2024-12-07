package org.betterbox.setsGenerator;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileManager {
    private final JavaPlugin plugin;
    private final SetsGenerator setsGenerator;
    private File dataFile;
    private FileConfiguration dataConfig;
    private PluginLogger pluginLogger;
    public String folderPath;

    public FileManager(String folderPath, JavaPlugin plugin, SetsGenerator setsGenerator, PluginLogger pluginLogger) {
        // pluginLogger.log(PluginLogger.LogLevel.CUSTOM_MOBS, "Event.onEntityDamageByEntity
        this.plugin = plugin;
        this.setsGenerator = setsGenerator;
        this.pluginLogger = pluginLogger;
        this.folderPath=folderPath;
        File logFolder = new File(folderPath, "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
        dataFile = new File(folderPath, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveItemStackToFile(String fileName, ItemStack itemStack) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "FileRewardManager.saveItemStackToFile called, fileName: " + fileName);
        File customRewardsFolder = new File(plugin.getDataFolder() + File.separator + "upgradeItems");
        if (!customRewardsFolder.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.INFO, "upgradeItems folder does not exist, creating a new one");
            customRewardsFolder.mkdirs();
        }
        try {
            File rewardFile = new File(customRewardsFolder, fileName + ".yml");
            pluginLogger.log(PluginLogger.LogLevel.INFO, "rewardItem will be saved to " + fileName + ".yml");
            rewardFile.createNewFile();
            FileConfiguration rewardFileConfig = YamlConfiguration.loadConfiguration(rewardFile);
            rewardFileConfig.set("item",itemStack);
            rewardFileConfig.save(rewardFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "rewardItem " + fileName + " saved");

        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Cannot save the item : " + fileName + ", error: " + e.getMessage());
        }
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
    public boolean playerExists(UUID playerId) {
        return dataConfig.getConfigurationSection("players." + playerId.toString()) != null;
    }

    public Map<String, Integer> loadPlayerEquipmentLevels(UUID playerId) {
        Map<String, Integer> equipmentLevels = new HashMap<>();
        String path = "players." + playerId.toString();

        if (!playerExists(playerId)) {
            return equipmentLevels; // Zwróci pustą mapę, jeżeli gracz nie istnieje
        }

        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
        for (String tag : tags) {
            int level = dataConfig.getInt(path + "." + tag, 0);
            equipmentLevels.put(tag, level);
        }

        return equipmentLevels;
    }

    public void savePlayerEquipmentLevels(UUID playerId, Map<String, Integer> equipmentLevels) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Saving equipment levels for player: " + playerId);

        String path = "players." + playerId.toString();
        for (Map.Entry<String, Integer> entry : equipmentLevels.entrySet()) {
            String fullPath = path + "." + entry.getKey();
            Integer value = entry.getValue();
            dataConfig.set(fullPath, value);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Set " + fullPath + " to " + value);
        }
        saveDataConfig();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Data config saved for player: " + playerId);
    }

    public void updatePlayerEquipmentLevel(UUID playerId, String tag, int newLevel) {
        String path = "players." + playerId.toString() + "." + tag;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Updating equipment level for player: " + playerId + ", tag: " + tag + ", new level: " + newLevel);

        dataConfig.set(path, newLevel);
        saveDataConfig();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Data config updated and saved for player: " + playerId + ", tag: " + tag);
    }

    private void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while saving data config: " + e.getMessage());
        }
    }


}
