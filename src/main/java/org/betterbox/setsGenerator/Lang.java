package org.betterbox.setsGenerator;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Lang {
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    public String swordName = "Blade Tier";
    public String chestplateName = "Chestplate Tier";
    public String leggingsName = "Leggings Tier";
    public String helmetName = "Helmet Tier";
    public String bootsName = "Boots Tier";
    public String talismanName = "Talisman Tier";
    public String elytraName = "Elytra Tier";

    public String maxLeevelReachedMessage = "Max level reached!";
    public String noPermissionMessage = "You don't have permission to use this command!";
    public String notEnoughItemsMessage2 = "Not enough required items! Required items";
    public String requiredItemsMessage = "Required items:";
    public String leftClickToUpgradeMessage = "Left-click to upgrade";
    public String selectUpgradeMessage = "Select upgrade";
    public String confirmMessage = "Confirm";
    public String confirmUpgradeMessage = "Confirm upgrade";
    public String cancelMessage = "Cancel";
    public String getNotEnoughItemsMessage = "Not enough items to upgrade!";
    public String welcomeMessage = "Witaj w serwerze! Otrzymałeś podstawowe wyposażenie.";
    public String eqUpdatedMessage = "Witaj ponownie! Twoje wyposażenie zostało zaktualizowane.";
    public String upgradeConfirmMessage = "Upgrade confirmed!";
    public String noItemFoundMessage = "No item found to upgrade.";
    public String thisItemCannotBeUpgraded = "This item cannot be upgraded.";
    public String noEmptySlotMessage = "No empty slot found in inventory.";
    public String failedToUpgradeMessage = "Failed to upgrade item.";
    public String upgradeCancelledMessage = "Upgrade cancelled.";


    public Lang(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        loadLangFile();
    }

    public void loadLangFile() {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Lang.loadLangFile called", transactionID);
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang directory...", transactionID);
            langDir.mkdirs();
        }

        File langFile = new File(langDir, "lang.yml");
        if (!langFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            createDefaultLangFile(langFile, transactionID);
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading lang.yml file...", transactionID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        validateAndLoadConfig(config, langFile, transactionID);
    }

    private void createDefaultLangFile(File langFile, String transactionID) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            langFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            setDefaultValues(config);
            config.save(langFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file created successfully!", transactionID);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error creating lang.yml file: " + e.getMessage(), transactionID);
        }
    }

    private void setDefaultValues(FileConfiguration config) {
        config.set("swordName", swordName);
        config.set("chestplateName", chestplateName);
        config.set("leggingsName", leggingsName);
        config.set("helmetName", helmetName);
        config.set("bootsName", bootsName);
        config.set("talismanName", talismanName);
        config.set("elytraName", elytraName);
        config.set("maxLeevelReachedMessage", maxLeevelReachedMessage);
        config.set("noPermissionMessage", noPermissionMessage);
        config.set("notEnoughItemsMessage", notEnoughItemsMessage2);
        config.set("requiredItemsMessage", requiredItemsMessage);
        config.set("leftClickToUpgradeMessage", leftClickToUpgradeMessage);
        config.set("selectUpgradeMessage", selectUpgradeMessage);
        config.set("confirmMessage", confirmMessage);
        config.set("confirmUpgradeMessage", confirmUpgradeMessage);
        config.set("cancelMessage", cancelMessage);
        config.set("welcomeMessage", welcomeMessage);
        config.set("notEnoughItemsMessage", getNotEnoughItemsMessage);
        config.set("eqUpdatedMessage", eqUpdatedMessage);
        config.set("upgradeConfirmMessage", upgradeConfirmMessage);
        config.set("noItemFoundMessage", noItemFoundMessage);
        config.set("thisItemCannotBeUpgraded", thisItemCannotBeUpgraded);
        config.set("noEmptySlotMessage", noEmptySlotMessage);
        config.set("failedToUpgradeMessage", failedToUpgradeMessage);
        config.set("upgradeCancelledMessage", upgradeCancelledMessage);

    }

    private void validateAndLoadConfig(FileConfiguration config, File langFile, String transactionID) {
        boolean saveRequired = false;

        if (!config.contains("swordName")) {
            config.set("swordName", swordName);
            saveRequired = true;
        } else {
            swordName = config.getString("swordName");
        }

        if (!config.contains("chestplateName")) {
            config.set("chestplateName", chestplateName);
            saveRequired = true;
        } else {
            chestplateName = config.getString("chestplateName");
        }



        if (!config.contains("leggingsName")) {
            config.set("leggingsName", leggingsName);
            saveRequired = true;
        } else {
            leggingsName = config.getString("leggingsName");
        }

        if (!config.contains("helmetName")) {
            config.set("helmetName", helmetName);
            saveRequired = true;
        } else {
            helmetName = config.getString("helmetName");
        }

        if (!config.contains("bootsName")) {
            config.set("bootsName", bootsName);
            saveRequired = true;
        } else {
            bootsName = config.getString("bootsName");
        }

        if (!config.contains("talismanName")) {
            config.set("talismanName", talismanName);
            saveRequired = true;
        } else {
            talismanName = config.getString("talismanName");
        }

        if (!config.contains("elytraName")) {
            config.set("elytraName", elytraName);
            saveRequired = true;
        } else {
            elytraName = config.getString("elytraName");
        }

        if (!config.contains("maxLeevelReachedMessage")) {
            config.set("maxLeevelReachedMessage", maxLeevelReachedMessage);
            saveRequired = true;
        } else {
            maxLeevelReachedMessage = config.getString("maxLeevelReachedMessage");
        }

        if (!config.contains("noPermissionMessage")) {
            config.set("noPermissionMessage", noPermissionMessage);
            saveRequired = true;
        } else {
            noPermissionMessage = config.getString("noPermissionMessage");
        }

        if (!config.contains("notEnoughItemsMessage")) {
            config.set("notEnoughItemsMessage", notEnoughItemsMessage2);
            saveRequired = true;
        } else {
            notEnoughItemsMessage2 = config.getString("notEnoughItemsMessage2");
        }

        if (!config.contains("requiredItemsMessage")) {
            config.set("requiredItemsMessage", requiredItemsMessage);
            saveRequired = true;
        } else {
            requiredItemsMessage = config.getString("requiredItemsMessage");
        }

        if (!config.contains("leftClickToUpgradeMessage")) {
            config.set("leftClickToUpgradeMessage", leftClickToUpgradeMessage);
            saveRequired = true;
        } else {
            leftClickToUpgradeMessage = config.getString("leftClickToUpgradeMessage");
        }

        if (!config.contains("selectUpgradeMessage")) {
            config.set("selectUpgradeMessage", selectUpgradeMessage);
            saveRequired = true;
        } else {
            selectUpgradeMessage = config.getString("selectUpgradeMessage");
        }

        if (!config.contains("confirmMessage")) {
            config.set("confirmMessage", confirmMessage);
            saveRequired = true;
        } else {
            confirmMessage = config.getString("confirmMessage");
        }

        if (!config.contains("confirmUpgradeMessage")) {
            config.set("confirmUpgradeMessage", confirmUpgradeMessage);
            saveRequired = true;
        } else {
            confirmUpgradeMessage = config.getString("confirmUpgradeMessage");
        }

        if (!config.contains("cancelMessage")) {
            config.set("cancelMessage", cancelMessage);
            saveRequired = true;
        } else {
            cancelMessage = config.getString("cancelMessage");
        }

        if (!config.contains("welcomeMessage")) {
            config.set("welcomeMessage", welcomeMessage);
            saveRequired = true;
        } else {
            welcomeMessage = config.getString("welcomeMessage");
        }

        if (!config.contains("notEnoughItemsMessage")) {
            config.set("notEnoughItemsMessage", getNotEnoughItemsMessage);
            saveRequired = true;
        } else {
            getNotEnoughItemsMessage = config.getString("notEnoughItemsMessage");
        }

        if (!config.contains("eqUpdatedMessage")) {
            config.set("eqUpdatedMessage", eqUpdatedMessage);
            saveRequired = true;
        } else {
            eqUpdatedMessage = config.getString("eqUpdatedMessage");
        }

        if (!config.contains("upgradeConfirmMessage")) {
            config.set("upgradeConfirmMessage", upgradeConfirmMessage);
            saveRequired = true;
        } else {
            upgradeConfirmMessage = config.getString("upgradeConfirmMessage");
        }

        if (!config.contains("noItemFoundMessage")) {
            config.set("noItemFoundMessage", noItemFoundMessage);
            saveRequired = true;
        } else {
            noItemFoundMessage = config.getString("noItemFoundMessage");
        }

        if (!config.contains("thisItemCannotBeUpgraded")) {
            config.set("thisItemCannotBeUpgraded", thisItemCannotBeUpgraded);
            saveRequired = true;
        } else {
            thisItemCannotBeUpgraded = config.getString("thisItemCannotBeUpgraded");
        }

        if (!config.contains("noEmptySlotMessage")) {
            config.set("noEmptySlotMessage", noEmptySlotMessage);
            saveRequired = true;
        } else {
            noEmptySlotMessage = config.getString("noEmptySlotMessage");
        }

        if (!config.contains("failedToUpgradeMessage")) {
            config.set("failedToUpgradeMessage", failedToUpgradeMessage);
            saveRequired = true;
        } else {
            failedToUpgradeMessage = config.getString("failedToUpgradeMessage");
        }

        if (!config.contains("upgradeCancelledMessage")) {
            config.set("upgradeCancelledMessage", upgradeCancelledMessage);
            saveRequired = true;
        } else {
            upgradeCancelledMessage = config.getString("upgradeCancelledMessage");
        }
        if (saveRequired) {
            try {
                config.save(langFile);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file updated with missing values", transactionID);
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error saving lang.yml file: " + e.getMessage(), transactionID);
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file loaded successfully!", transactionID);
    }
}