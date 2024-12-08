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

import java.util.Map;

public class GuiManager {
    private final JavaPlugin plugin;
    private final ItemFactory itemFactory;
    private final PluginLogger pluginLogger;

    public GuiManager(JavaPlugin plugin, ItemFactory itemFactory,PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        this.pluginLogger=pluginLogger;
    }

    public void openLevelUpGui(Player player) {
        // Pobierz poziomy przedmiotów gracza
        SetsGenerator setsGenerator = (SetsGenerator) plugin;
        Map<String, Integer> equipmentLevels = setsGenerator.getPlayerEquipmentLevels(player);

        // Tworzenie GUI
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Select Upgrade");

        // Dodaj przedmioty +1 poziom
        for (Map.Entry<String, Integer> entry : equipmentLevels.entrySet()) {
            String itemType = entry.getKey();
            int currentLevel = entry.getValue();

            ItemStack upgradedItem = createUpgradedItem(itemType, currentLevel + 1,setsGenerator.hasRequiredItems(player,setsGenerator.getUpgradeItems(currentLevel + 1)));
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
        // Tworzenie GUI potwierdzającego
        Inventory confirmationGui = Bukkit.createInventory(null, 9, ChatColor.RED + "Confirm Upgrade");

        // Zielona wełna (akceptacja)
        ItemStack greenWool = new ItemStack(Material.GREEN_WOOL);
        ItemMeta greenMeta = greenWool.getItemMeta();
        if (greenMeta != null) {
            greenMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN));
            greenMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Confirm"), PersistentDataType.INTEGER, 1);
            greenWool.setItemMeta(greenMeta);
        }

// Czerwona wełna (anulowanie)
        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = redWool.getItemMeta();
        if (redMeta != null) {
            redMeta.displayName(Component.text("Cancel", NamedTextColor.RED));
            redMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "Cancel"), PersistentDataType.INTEGER, 1);
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