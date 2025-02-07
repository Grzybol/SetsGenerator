package org.betterbox.setsGenerator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GuiManager {
    private final JavaPlugin plugin;
    private final ItemFactory itemFactory;
    private final PluginLogger pluginLogger;
    private final Lang lang;

    public GuiManager(JavaPlugin plugin, ItemFactory itemFactory,PluginLogger pluginLogger, Lang lang) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.lang=lang;
        this.pluginLogger=pluginLogger;
    }
    private final List<String> tags = new ArrayList<>(Arrays.asList(
            "chestplate_level",
            "leggings_level",
            "helmet_level",
            "boots_level",
            "talisman_level",
            "sword_level"
    ));
    public void openLevelUpGui(Player player,String transactionID) {
        // Pobierz poziomy przedmiotów gracza
        SetsGenerator setsGenerator = (SetsGenerator) plugin;
        Map<String, Integer> equipmentLevels = setsGenerator.getPlayerEquipmentLevels(player);

        // Tworzenie GUI
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + lang.selectUpgradeMessage);

        // Przeglądaj każdy tag z listy
        for (String tag : tags) {
            String itemType = tag; // Zakładam, że itemType odpowiada tagowi

            if (equipmentLevels.containsKey(tag)) {
                // Gracz posiada ten tag, pobierz aktualny poziom
                int currentLevel = equipmentLevels.get(tag);

                // Sprawdź, czy gracz ma wymagane przedmioty do ulepszenia
                boolean hasRequired = setsGenerator.hasRequiredItems(player, setsGenerator.getUpgradeItems(currentLevel + 1),transactionID);

                // Utwórz przedmiot z kolejnym poziomem
                ItemStack upgradedItem = createUpgradedItem(itemType, currentLevel + 1, hasRequired);
                if (upgradedItem != null) {
                    gui.addItem(upgradedItem);
                }
            } else {
                boolean hasRequired = setsGenerator.hasRequiredItems(player, setsGenerator.getUpgradeItems(0),transactionID);
                // Gracz nie posiada tego tagu, utwórz przedmiot z poziomem 0 i flagą true
                ItemStack upgradedItem = createUpgradedItem(itemType, 0, hasRequired);
                if (upgradedItem != null) {
                    gui.addItem(upgradedItem);
                }
            }
        }

        // Otwórz GUI dla gracza
        player.openInventory(gui);
    }

    public void openLevelUpGuiOld(Player player) {
        String transactionID = UUID.randomUUID().toString();
        // Pobierz poziomy przedmiotów gracza
        SetsGenerator setsGenerator = (SetsGenerator) plugin;
        Map<String, Integer> equipmentLevels = setsGenerator.getPlayerEquipmentLevels(player);

        // Tworzenie GUI
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + lang.selectUpgradeMessage);

        // Dodaj przedmioty +1 poziom
        for (Map.Entry<String, Integer> entry : equipmentLevels.entrySet()) {
            String itemType = entry.getKey();
            int currentLevel = entry.getValue();

            ItemStack upgradedItem = createUpgradedItem(itemType, currentLevel + 1,setsGenerator.hasRequiredItems(player,setsGenerator.getUpgradeItems(currentLevel + 1),transactionID));
            if (upgradedItem != null) {
                gui.addItem(upgradedItem);
            }
        }

        // Otwórz GUI dla gracza
        player.openInventory(gui);
    }

    private ItemStack createUpgradedItem(String itemType, int level, boolean hasRequiredItems) {
        switch (itemType) {
            case "chestplate_level":
                return itemFactory.createChestplate(level,true,hasRequiredItems);
            case "leggings_level":
                return itemFactory.createLeggings(level,true,hasRequiredItems);
            case "helmet_level":
                return itemFactory.createHelmet(level,true,hasRequiredItems);
            case "boots_level":
                return itemFactory.createBoots(level,true,hasRequiredItems);
            case "talisman_level":
                return itemFactory.createTalisman(level,true,hasRequiredItems);
            case "sword_level":
                return itemFactory.createSword(level,true,hasRequiredItems);
            default:
                return null;
        }
    }

    public void openConfirmationGui(Player player, ItemStack selectedItem) {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManagert.openConfirmationGui for player: " + player.getName()+", selectedItem: "+selectedItem.toString(),transactionID);
        // Tworzenie GUI potwierdzającego
        Inventory confirmationGui = Bukkit.createInventory(null, 9, ChatColor.RED + lang.confirmUpgradeMessage);

        // Zielona wełna (akceptacja)
        ItemStack greenWool = new ItemStack(Material.GREEN_WOOL);
        ItemMeta greenMeta = greenWool.getItemMeta();
        if (greenMeta != null) {
            greenMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN));
            greenMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, lang.confirmMessage), PersistentDataType.INTEGER, 1);
            greenWool.setItemMeta(greenMeta);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "GuiManagert.openConfirmationGui for player: " + player.getName()+" confirmed",transactionID);
        }

// Czerwona wełna (anulowanie)
        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = redWool.getItemMeta();
        if (redMeta != null) {
            redMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
            redMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, lang.cancelMessage), PersistentDataType.INTEGER, 1);
            redWool.setItemMeta(redMeta);
        }

        // Dodanie elementów do GUI
        confirmationGui.setItem(3, greenWool);
        confirmationGui.setItem(5, redWool);
        confirmationGui.setItem(4, selectedItem);
        // Otwórz GUI dla gracza
        player.openInventory(confirmationGui);
    }
}