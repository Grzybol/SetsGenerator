package org.betterbox.setsGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
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
    private final ConfigManager configManager;

    // Mapa do przechowywania wybranego przedmiotu przez gracza pomiędzy GUI "Select Upgrade" a "Confirm Upgrade"
    private final Map<UUID, String> playerSelectedUpgradeItem = new HashMap<>();
    //private final Map<UUID, ItemStack> playerSelectedUpgradeItem = new HashMap<>();
    private final Lang lang;

    public EventManager(JavaPlugin plugin, GuiManager guiManager, ItemFactory itemFactory, SetsGenerator setsGenerator, PluginLogger pluginLogger, FileManager fileManager, ConfigManager configManager, Lang lang) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.lang = lang;
        this.fileManager = fileManager;
        this.guiManager = guiManager;
        this.configManager = configManager;
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
    @EventHandler
    public void onPlayerInteractWithNPC(PlayerInteractEntityEvent event) {
        String transactionID = UUID.randomUUID().toString();
        if (event.getRightClicked() instanceof Villager villager) {
            NamespacedKey key = new NamespacedKey(plugin, "SetsGeneratorShop");
            if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                if(event.getPlayer().isSneaking() && event.getPlayer().isOp()){
                    villager.remove();
                }
                Player player = event.getPlayer();
                Map<String, Integer> currentEqLevels = setsGenerator.getPlayerEquipmentLevels(player);


                //player.sendMessage(ChatColor.GREEN + "You have interacted with the Shop NPC!");
                guiManager.openLevelUpGui(player,transactionID);
                event.setCancelled(true);
            }
        }
    }
    /*
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Map<String, Integer> currentEqLevels = setsGenerator.getPlayerEquipmentLevels(player);

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

     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerJoin: Player joined",transactionID);
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Sprawdzenie, czy gracz istnieje w bazie danych
        if (!fileManager.playerExists(playerId)) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerJoin: New player joined",transactionID);
            // **Nowy gracz**: Inicjalizacja poziomów na 0 i przydzielenie ekwipunku
            Map<String, Integer> defaultLevels = new HashMap<>();
            for (String tag : tags) {
                defaultLevels.put(tag, 0);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerJoin: tag: "+tag,transactionID);
            }

            fileManager.savePlayerEquipmentLevels(playerId, defaultLevels,transactionID);

            // Przygotowanie mapy funkcji do tworzenia przedmiotów na poziomie 0
            Map<String, Function<Integer, ItemStack>> creationMethods = new HashMap<>();
            creationMethods.put("chestplate_level", level -> itemFactory.createChestplate(level));
            creationMethods.put("leggings_level", level -> itemFactory.createLeggings(level));
            creationMethods.put("helmet_level", level -> itemFactory.createHelmet(level));
            creationMethods.put("boots_level", level -> itemFactory.createBoots(level));
            creationMethods.put("talisman_level", level -> itemFactory.createTalisman(level));
            creationMethods.put("sword_level", level -> itemFactory.createSword(level));

            // Przydzielenie przedmiotów do ekwipunku gracza
            for (String tag : tags) {
                ItemStack item = creationMethods.get(tag).apply(0);
                player.getInventory().addItem(item);
            }

            // Informacja dla gracza (opcjonalne)
            player.sendMessage(ChatColor.GREEN + lang.welcomeMessage);
        } else {
            Map<String, Integer> currentEqLevels = setsGenerator.getPlayerEquipmentLevels(player);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerJoin: Existing player joined, player "+player.getName()+", currentEqLevels: "+currentEqLevels.toString(),transactionID);
            // **Istniejący gracz**: Sprawdzenie obecnego ekwipunku i aktualizacja bazy danych
            // Pobranie obecnych poziomów ekwipunku z gracza


            // Zapisanie aktualnych poziomów ekwipunku do bazy danych
            fileManager.savePlayerEquipmentLevels(playerId, currentEqLevels,transactionID);

            // (Opcjonalnie) Synchronizacja ekwipunku gracza z poziomami w bazie danych
            // Może to obejmować aktualizację przedmiotów w ekwipunku na podstawie poziomów
            // Jeśli chcesz, aby ekwipunek gracza był zawsze zgodny z poziomami w bazie,
            // możesz dodać poniższy fragment kodu.

        /*
        Map<String, Function<Integer, ItemStack>> creationMethods = new HashMap<>();
        creationMethods.put("chestplate_level", level -> itemFactory.createChestplate(level));
        creationMethods.put("leggings_level", level -> itemFactory.createLeggings(level));
        creationMethods.put("helmet_level", level -> itemFactory.createHelmet(level));
        creationMethods.put("boots_level", level -> itemFactory.createBoots(level));
        creationMethods.put("talisman_level", level -> itemFactory.createTalisman(level));
        creationMethods.put("sword_level", level -> itemFactory.createSword(level));

        for (String tag : tags) {
            int level = currentEqLevels.getOrDefault(tag, 0);
            // Sprawdzanie, czy gracz ma przedmiot z tym tagiem
            boolean hasItem = hasItemWithTag(player, tag);

            if (!hasItem) {
                // Dodanie brakującego przedmiotu na podstawie poziomu
                player.getInventory().addItem(creationMethods.get(tag).apply(level));
            }
            // Opcjonalnie: aktualizacja przedmiotów, które już istnieją (np. zwiększenie poziomu)
            // Można tutaj dodać dodatkową logikę, jeśli jest to potrzebne
        }
        */

            // Informacja dla gracza (opcjonalne)
            player.sendMessage(ChatColor.GREEN + lang.eqUpdatedMessage);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            NamespacedKey key = setsGenerator.getKey();
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

    /*
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

     */

    private boolean hasRestrictedTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        for (String tag : tags) {
            if (item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, tag), PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    /*
    // Blokowanie wyrzucania przedmiotów
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Zapisanie stanu ekwipunku przed jakąkolwiek modyfikacją
        ItemStack[] backupInv = player.getInventory().getContents().clone();
        ItemStack[] backupArmor = player.getInventory().getArmorContents().clone();

        ItemStack item = event.getItemDrop().getItemStack();
        if (hasRestrictedTag(item)) {
            // Anulujemy event - uniemożliwiamy wyrzucenie przedmiotu
            event.setCancelled(true);

            // Przywracamy stan ekwipunku do tego sprzed próby upuszczenia
            player.getInventory().setContents(backupInv);
            player.getInventory().setArmorContents(backupArmor);

            // Wymuszamy aktualizację ekwipunku po stronie klienta
            player.updateInventory();
            player.sendMessage(ChatColor.RED + "You cannot drop this item!");

            // Po 1 ticku ponownie przywracamy stan ekwipunku i aktualizujemy go
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.getInventory().setContents(backupInv);
                player.getInventory().setArmorContents(backupArmor);
                player.updateInventory();
            }, 1L);
        }
    }

    */

    /*
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getOldCursor();

        if (hasRestrictedTag(item)) {
            // Jeśli którakolwiek ze slotów, do których gracz próbuje przeciągnąć przedmiot
            // nie należy do ekwipunku gracza, anuluj zdarzenie
            for (int slot : event.getRawSlots()) {
                // W ekwipunku gracza sloty są liczone od zera do jego rozmiaru.
                // Sloty powyżej rozmiaru własnego ekwipunku to zwykle górne GUI (np. skrzynka)
                if (slot >= player.getInventory().getSize()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot drag this restricted item here!");
                    break;
                }
            }
        }
    }

     */
    @EventHandler
    public void onEntityLoad(EntitySpawnEvent event) {
        String transactionID = UUID.randomUUID().toString();
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onEntityLoad: Villager spawned, villager.getPersistentDataContainer(): "+villager.getPersistentDataContainer()+", UUID: "+villager.getUniqueId()+", displayname: "+villager.getCustomName(), UUID.randomUUID().toString());
            NamespacedKey key = new NamespacedKey(plugin, "SetsGeneratorShop");
            if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING) || configManager.getVillagerUUIDs().contains(villager.getUniqueId()) || (villager.getCustomName() != null && villager.getCustomName().equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Equipment upgrades"))) {
                configManager.setupVillager(villager,key,transactionID);
            }else{
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onEntityLoad: Villager nie jest na liście naszych villagerów");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        String transactionID = UUID.randomUUID().toString();
        Player player = (Player) event.getWhoClicked();

        if (player.hasMetadata("handledUpgradeClick")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Event.onPlayerDeath event already handled! victim: "+player.getName(),transactionID);
            return;
        }
        player.setMetadata("handledUpgradeClick", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.removeMetadata("handledUpgradeClick", plugin), 1L);

        ItemStack clickedItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();
        // Sprawdź czy przedmiot jest restrykcyjny
        if (hasRestrictedTag(clickedItem)) {
            // Sprawdź, czy gracze próbują przesunąć przedmiot do ekwipunku innego niż własny
            // BottomInventory to ekwipunek gracza, top (clickedInventory inny niż player) to zwykle skrzynka.
            if (!clickedInventory.equals(player.getInventory())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot move this restricted item here!");
            }
        }



        // Sprawdzamy tytuł okna, aby rozpoznać nasze GUI
        String title = event.getView().getTitle();
        boolean isSelectUpgradeGui = title.equalsIgnoreCase(ChatColor.GREEN + lang.selectUpgradeMessage);
        boolean isConfirmUpgradeGui = title.equalsIgnoreCase(ChatColor.RED + lang.confirmUpgradeMessage);
        int selectedItemLevel = 0;
        selectedItemLevel=setsGenerator.getItemLevel(clickedItem);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: title " + title+", isSelectUpgradeGui: "+isSelectUpgradeGui+", isConfirmUpgradeGui: "+isConfirmUpgradeGui+", selectedItemLevel: "+selectedItemLevel,transactionID);
        // Jeśli to jedno z naszych GUI, zablokuj wyciąganie przedmiotów
        if (isSelectUpgradeGui || isConfirmUpgradeGui) {

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: clickedItem is null or AIR",transactionID);
                return; // Brak interakcji
            }
            if(!setsGenerator.hasAnyTag(clickedItem)){
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: clickedItem has no tag",transactionID);
                event.setCancelled(true);
                return;
            }
            if(selectedItemLevel>setsGenerator.getLoadedLevels()){
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: clickedItem has higher level than loaded levels",transactionID);
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            // Jeśli to GUI "Select Upgrade"
            if (isSelectUpgradeGui) {


                String tag = setsGenerator.getTagFromItemstack(clickedItem);
                // Zapamiętujemy wybrany przedmiot
                playerSelectedUpgradeItem.put(player.getUniqueId(), tag);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: player.getUniqueId()) " + player.getUniqueId()+", clickedItem: "+ clickedItem+", tag: "+tag,transactionID);

                if(setsGenerator.isFromUpgradeGUI(clickedItem)){
                    boolean hasRequiredItems = setsGenerator.hasRequiredItems(player, setsGenerator.getUpgradeItems(selectedItemLevel),transactionID);
                    if(!hasRequiredItems){
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Not enough items to upgrade! selectedItemLevel "+selectedItemLevel,transactionID);
                        player.sendMessage(ChatColor.RED + lang.getNotEnoughItemsMessage);
                        return;
                    }
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: This item is from upgrade GUI!",transactionID);
                    guiManager.openConfirmationGui(player, clickedItem);
                }else {
                    boolean hasRequiredItems = setsGenerator.hasRequiredItems(player, setsGenerator.getUpgradeItems(selectedItemLevel + 1),transactionID);
                    if(!hasRequiredItems){
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Not enough items to upgrade!",transactionID);
                        player.sendMessage(ChatColor.RED + lang.getNotEnoughItemsMessage);
                        return;
                    }
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: This item is not from upgrade GUI!",transactionID);
                    guiManager.openConfirmationGui(player, itemFactory.getNextLevel(clickedItem,hasRequiredItems));
                    //player.sendMessage(ChatColor.RED + "This item cannot be upgraded.");
                }

            }
            // Jeśli to GUI "Confirm Upgrade"
            else if (isConfirmUpgradeGui) {

                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: player.getUniqueId()) " + player.getUniqueId()+", clickedItem: "+ clickedItem,transactionID);
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return; // Brak interakcji
                }

                String displayName = clickedItem.getItemMeta().getDisplayName();
                if ((ChatColor.GREEN + lang.confirmMessage).equalsIgnoreCase(displayName)) {
                    player.sendMessage(ChatColor.GREEN + lang.upgradeConfirmMessage);
                    player.closeInventory();

                    // Pobieramy wcześniej wybrany przedmiot
                    //ItemStack upgradeItem = playerSelectedUpgradeItem.get(player.getUniqueId());
                    String tag = playerSelectedUpgradeItem.get(player.getUniqueId());
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: recovered tag: " + tag,transactionID);


                    if (tag == null) {
                        player.sendMessage(ChatColor.RED + lang.noItemFoundMessage);
                        return;
                    }

                    // Znalezienie slotu, w którym znajduje się przedmiot do ulepszenia
                    int slot = setsGenerator.getItemSlotFromPlayerEqByTag(player,tag,transactionID);
                    /*if (slot == -1) {
                        // Jeżeli nie znaleziono przedmiotu w eq, można obsłużyć tę sytuację inaczej
                        player.sendMessage(ChatColor.RED + "Item to upgrade is not in your inventory.");
                        return;
                    }

                     */

                    // Implementacja ulepszenia
                    ItemStack upgradeItem = setsGenerator.getItemFromPlayerEqByTag(player,tag,transactionID);

                    int currentLevel = setsGenerator.getItemLevel(upgradeItem);
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: currentLevel: " + currentLevel,transactionID);
                    int newLevel=0;
                    if(currentLevel!=-1){
                        newLevel = currentLevel + 1;
                    }
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: newLevel: " + newLevel,transactionID);



                    if(!setsGenerator.checkAndRemoveItems(player,setsGenerator.getUpgradeItems(newLevel),transactionID)){
                        player.sendMessage(ChatColor.RED + lang.getNotEnoughItemsMessage);
                        return;
                    }
                    fileManager.updatePlayerEquipmentLevel(player.getUniqueId(),tag,newLevel,transactionID);
                    ItemStack newItem = null;

                    if(upgradeItem==null) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: upgradeItem is null", transactionID);
                        switch (tag) {
                            case "helmet_level":
                                newItem = itemFactory.createHelmet(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new helmet with level " + newLevel, transactionID);
                                break;
                            case "sword_level":
                                newItem = itemFactory.createSword(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new sword with level " + newLevel, transactionID);
                                break;
                            case "talisman_level":
                                newItem = itemFactory.createTalisman(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new talisman with level " + newLevel, transactionID);
                                break;
                            case "leggings_level":
                                newItem = itemFactory.createLeggings(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new leggings with level " + newLevel, transactionID);
                                break;
                            case "chestplate_level":
                                newItem = itemFactory.createChestplate(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new chestplate with level " + newLevel, transactionID);
                                break;
                            case "boots_level":
                                newItem = itemFactory.createBoots(newLevel);
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new boots with level " + newLevel, transactionID);
                                break;
                            default:
                                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: This item type cannot be upgraded.", transactionID);
                                player.sendMessage(ChatColor.RED + lang.thisItemCannotBeUpgraded);
                                return;
                        }
                    }else {
                            switch (upgradeItem.getType()) {
                                case LEATHER_HELMET:
                                    newItem = itemFactory.createHelmet(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new helmet with level " + newLevel, transactionID);
                                    break;
                                case DIAMOND_SWORD:
                                    newItem = itemFactory.createSword(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new sword with level " + newLevel, transactionID);
                                    break;
                                case MAGMA_CREAM:
                                    newItem = itemFactory.createTalisman(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new talisman with level " + newLevel, transactionID);
                                    break;
                                case LEATHER_LEGGINGS:
                                    newItem = itemFactory.createLeggings(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new leggings with level " + newLevel, transactionID);
                                    break;
                                case ELYTRA:
                                    newItem = itemFactory.createChestplate(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new chestplate with level " + newLevel, transactionID);
                                    break;
                                case LEATHER_BOOTS:
                                    newItem = itemFactory.createBoots(newLevel);
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new boots with level " + newLevel, transactionID);
                                    break;
                                default:
                                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: This item type cannot be upgraded.", transactionID);
                                    player.sendMessage(ChatColor.RED + lang.thisItemCannotBeUpgraded);
                                    return;
                            }

                    }
                    if (newItem == null) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "EventManager.onPlayerClick: newItem is null, upgrade failed.");
                        player.sendMessage(ChatColor.RED + lang.failedToUpgradeMessage);
                        return;
                    }

// Ustawiamy nowy przedmiot w slocie gracza
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Setting upgraded item in player's inventory at slot " + slot,transactionID);
                    if(slot==-1){
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Slot "+slot,transactionID);
                        Inventory inventory = player.getInventory();
                        int firstEmpty = inventory.firstEmpty();
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: firstEmpty "+firstEmpty,transactionID);
                        if(firstEmpty==-1){
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: No empty slot to put upgraded item.",transactionID);
                            player.sendMessage(ChatColor.RED + lang.noEmptySlotMessage);
                            return;
                        }else {
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Setting upgraded item in player's inventory at slot " + firstEmpty,transactionID);
                            player.getInventory().setItem(firstEmpty, newItem);
                            return;
                        }
                    }else {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Setting upgraded item in player's inventory at slot " + slot,transactionID);
                        player.getInventory().setItem(slot, newItem);
                    }

// Opcjonalnie można wymusić aktualizację ekwipunku (w niektórych przypadkach pomocne)
                    player.updateInventory();
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Player inventory updated.",transactionID);

// Dodatkowa weryfikacja
                    ItemStack checkItem = player.getInventory().getItem(slot);
                    if (checkItem != null && checkItem.equals(newItem)) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Upgrade successful. New item is now in the slot.",transactionID);
                    } else {
                        pluginLogger.log(PluginLogger.LogLevel.WARNING, "EventManager.onPlayerClick: Upgrade attempt did not reflect in player's inventory. Check itemFactory and item creation methods.",transactionID);
                    }



                } else if ((ChatColor.RED + lang.cancelMessage).equalsIgnoreCase(displayName)) {
                    player.sendMessage(ChatColor.RED + lang.upgradeCancelledMessage);
                    player.closeInventory();
                    playerSelectedUpgradeItem.remove(player.getUniqueId());
                }else{
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: confirm failed",transactionID);
                    // Czyszczenie z mapy po ulepszeniu
                    playerSelectedUpgradeItem.remove(player.getUniqueId());
                }
            }
        }
    }
}