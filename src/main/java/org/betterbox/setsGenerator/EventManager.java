package org.betterbox.setsGenerator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class EventManager implements Listener {

    private final JavaPlugin plugin;

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Lista tagów, które chcemy sprawdzać
    private final String[] tags = {
            "chestplate_level",
            "leggings_level",
            "helmet_level",
            "boots_level",
            "talisman_level",
            "sword_level"
    };

    private boolean hasRestrictedTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        for (String tag : tags) {
            if (item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, tag), PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    // Blokowanie wyrzucania przedmiotów
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (hasRestrictedTag(item)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.RED + "You cannot drop this item!");
        }
    }

    // Blokowanie przenoszenia przedmiotów między ekwipunkami
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (hasRestrictedTag(item)) {
            event.setCancelled(true);
        }
    }

    // Blokowanie podnoszenia przedmiotów z ziemi
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (hasRestrictedTag(item)) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                player.sendMessage(ChatColor.RED + "You cannot pick up this item!");
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return; // Pozwól na interakcje poza ekwipunkiem gracza
        }

        ItemStack item = event.getCurrentItem();
        if (hasRestrictedTag(item)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                player = (Player) event.getWhoClicked();
                player.sendMessage(ChatColor.RED + "You cannot move this item!");
            }
        }

        if (inventory == null || !event.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select Upgrade") &&
                !event.getView().getTitle().equalsIgnoreCase(ChatColor.RED + "Confirm Upgrade")) {
            return; // Nie dotyczy naszego GUI
        }

        event.setCancelled(true); // Blokowanie interakcji z GUI

        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select Upgrade")) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return; // Brak interakcji
            }

            GuiManager guiManager = new GuiManager(plugin, ((SetsGenerator) plugin).getItemFactory());
            guiManager.openConfirmationGui(player, clickedItem);
        }

        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.RED + "Confirm Upgrade")) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return; // Brak interakcji
            }

            String displayName = clickedItem.getItemMeta().getDisplayName();
            if ((ChatColor.GREEN + "Confirm").equalsIgnoreCase(displayName)) {
                player.sendMessage(ChatColor.GREEN + "Upgrade confirmed!");
                player.closeInventory();
                // Implementacja ulepszenia
            } else if ((ChatColor.RED + "Cancel").equalsIgnoreCase(displayName)) {
                player.sendMessage(ChatColor.RED + "Upgrade canceled.");
                player.closeInventory();
            }
        }
    }
}
