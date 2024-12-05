package org.betterbox.setsGenerator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class EventManager implements Listener {

    private final JavaPlugin plugin;
    private final GuiManager guiManager;
    private final ItemFactory itemFactory;
    private final SetsGenerator setsGenerator;
    private final PluginLogger pluginLogger;
    private final FileManager fileManager;

    // Mapa do przechowywania wybranego przedmiotu przez gracza pomiędzy GUI "Select Upgrade" a "Confirm Upgrade"
    private final Map<UUID, ItemStack> playerSelectedUpgradeItem = new HashMap<>();

    public EventManager(JavaPlugin plugin, GuiManager guiManager, ItemFactory itemFactory, SetsGenerator setsGenerator, PluginLogger pluginLogger, FileManager fileManager) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.fileManager = fileManager;
        this.guiManager = guiManager;
        this.setsGenerator = setsGenerator;
        this.itemFactory = itemFactory;
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

    /*
    @EventHandler
    public void onPlayerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            // Sprawdzenie, czy NPC ma odpowiedni tag w metadanych
            if (villager.hasMetadata("SetsGeneratorShop")) {
                for (MetadataValue meta : villager.getMetadata("SetsGeneratorShop")) {
                    if (meta.asString().equals("SetsGenerator Shop") && meta.getOwningPlugin() == this.plugin) {
                        Player player = event.getPlayer();
                        player.sendMessage(ChatColor.GREEN + "You have interacted with the Shop NPC!");
                        guiManager.openLevelUpGui(player);
                        event.setCancelled(true); // Zatrzymuje dalszą interakcję z NPC
                        break;
                    }
                }
            }
        }
    }

     */
    @EventHandler
    public void onPlayerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            NamespacedKey key = new NamespacedKey(plugin, "SetsGeneratorShop");
            if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.GREEN + "You have interacted with the Shop NPC!");
                guiManager.openLevelUpGui(player);
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!fileManager.playerExists(playerId)) {
            Map<String, Integer> defaultLevels = new HashMap<>();
            for (String tag : tags) {
                defaultLevels.put(tag, 0);
            }
            fileManager.savePlayerEquipmentLevels(playerId, defaultLevels);
        }

        // Wczytujemy poziomy z bazy
        Map<String, Integer> equipmentLevels = fileManager.loadPlayerEquipmentLevels(playerId);

        // Przygotowujemy mapę funkcji do tworzenia przedmiotów na określonym poziomie
        Map<String, Function<Integer, ItemStack>> creationMethods = new HashMap<>();
        creationMethods.put("chestplate_level", level -> itemFactory.createChestplate(level));
        creationMethods.put("leggings_level", level -> itemFactory.createLeggings(level));
        creationMethods.put("helmet_level", level -> itemFactory.createHelmet(level));
        creationMethods.put("boots_level", level -> itemFactory.createBoots(level));
        creationMethods.put("talisman_level", level -> itemFactory.createTalisman(level));
        creationMethods.put("sword_level", level -> itemFactory.createSword(level));

        boolean updated = false;

        for (String tag : tags) {
            int level = equipmentLevels.getOrDefault(tag, 0);

            // Sprawdzamy, czy gracz ma już przedmiot z tym tagiem w ekwipunku
            boolean hasItem = hasItemWithTag(player, tag);

            // Jeśli poziom = 0 i nie ma przedmiotu, to go dodajemy i ustawiamy poziom na 1
            if (level == 0 && !hasItem) {
                player.getInventory().addItem(creationMethods.get(tag).apply(0));
                equipmentLevels.put(tag, 1);
                updated = true;
            }
            // Jeśli poziom > 0, a gracz nie ma przedmiotu w ekwipunku, musimy go ponownie przyznać na podstawie zapisanego poziomu
            else if (level > 0 && !hasItem) {
                player.getInventory().addItem(creationMethods.get(tag).apply(level));
                // Tutaj nie zmieniamy poziomu, bo zakładamy, że poziom był już poprawnie ustawiony
                // Można jednak rozważyć aktualizację poziomu, jeśli logika tego wymaga
            }
        }

        if (updated) {
            fileManager.savePlayerEquipmentLevels(playerId, equipmentLevels);
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            NamespacedKey key = new NamespacedKey(plugin, "SetsGeneratorShop");
            if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                // To jest nasz NPC - anulujemy obrażenia
                event.setCancelled(true);
            }
        }
    }

    private boolean hasItemWithTag(Player player, String tag) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() &&
                    item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, tag), PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Ładujemy poziomy z bazy
        Map<String, Integer> equipmentLevels = fileManager.loadPlayerEquipmentLevels(playerId);

        Map<String, Function<Integer, ItemStack>> creationMethods = new HashMap<>();
        creationMethods.put("chestplate_level", level -> itemFactory.createChestplate(level));
        creationMethods.put("leggings_level", level -> itemFactory.createLeggings(level));
        creationMethods.put("helmet_level", level -> itemFactory.createHelmet(level));
        creationMethods.put("boots_level", level -> itemFactory.createBoots(level));
        creationMethods.put("talisman_level", level -> itemFactory.createTalisman(level));
        creationMethods.put("sword_level", level -> itemFactory.createSword(level));

        // Przywracamy przedmioty na podstawie danych z bazy
        for (String tag : tags) {
            int level = equipmentLevels.getOrDefault(tag, 0);
            if (level == 0) {
                player.getInventory().addItem(creationMethods.get(tag).apply(0));
            }
        }
    }

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
            if (event.getEntity() instanceof Player player) {
                player.sendMessage(ChatColor.RED + "You cannot pick up this item!");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Sprawdzamy tytuł okna, aby rozpoznać nasze GUI
        String title = event.getView().getTitle();
        boolean isSelectUpgradeGui = title.equalsIgnoreCase(ChatColor.GREEN + "Select Upgrade");
        boolean isConfirmUpgradeGui = title.equalsIgnoreCase(ChatColor.RED + "Confirm Upgrade");

        // Jeśli to jedno z naszych GUI, zablokuj wyciąganie przedmiotów
        if (isSelectUpgradeGui || isConfirmUpgradeGui) {
            event.setCancelled(true);
            // Jeśli to GUI "Select Upgrade"
            if (isSelectUpgradeGui) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return; // Brak interakcji
                }
                // Zapamiętujemy wybrany przedmiot
                playerSelectedUpgradeItem.put(player.getUniqueId(), clickedItem.clone());
                guiManager.openConfirmationGui(player, clickedItem);
            }
            // Jeśli to GUI "Confirm Upgrade"
            else if (isConfirmUpgradeGui) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return; // Brak interakcji
                }

                String displayName = clickedItem.getItemMeta().getDisplayName();
                if ((ChatColor.GREEN + "Confirm").equalsIgnoreCase(displayName)) {
                    player.sendMessage(ChatColor.GREEN + "Upgrade confirmed!");
                    player.closeInventory();

                    // Pobieramy wcześniej wybrany przedmiot
                    ItemStack upgradeItem = playerSelectedUpgradeItem.get(player.getUniqueId());
                    if (upgradeItem == null) {
                        player.sendMessage(ChatColor.RED + "No item found to upgrade.");
                        return;
                    }

                    // Znalezienie slotu, w którym znajduje się przedmiot do ulepszenia
                    int slot = player.getInventory().first(upgradeItem);
                    if (slot == -1) {
                        // Jeżeli nie znaleziono przedmiotu w eq, można obsłużyć tę sytuację inaczej
                        player.sendMessage(ChatColor.RED + "Item to upgrade is not in your inventory.");
                        return;
                    }

                    // Implementacja ulepszenia
                    switch (upgradeItem.getType()) {
                        case LEATHER_HELMET:
                            player.getInventory().setItem(slot, itemFactory.createHelmet(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        case DIAMOND_SWORD:
                            player.getInventory().setItem(slot, itemFactory.createSword(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        case MAGMA_CREAM:
                            player.getInventory().setItem(slot, itemFactory.createTalisman(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        case LEATHER_LEGGINGS:
                            player.getInventory().setItem(slot, itemFactory.createLeggings(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        case LEATHER_CHESTPLATE:
                            player.getInventory().setItem(slot, itemFactory.createChestplate(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        case LEATHER_BOOTS:
                            player.getInventory().setItem(slot, itemFactory.createBoots(setsGenerator.getItemLevel(upgradeItem) + 1));
                            break;
                        default:
                            player.sendMessage(ChatColor.RED + "This item cannot be upgraded.");
                    }

                    // Czyszczenie z mapy po ulepszeniu
                    playerSelectedUpgradeItem.remove(player.getUniqueId());

                } else if ((ChatColor.RED + "Cancel").equalsIgnoreCase(displayName)) {
                    player.sendMessage(ChatColor.RED + "Upgrade canceled.");
                    player.closeInventory();
                    playerSelectedUpgradeItem.remove(player.getUniqueId());
                }
            }
        }
    }
}