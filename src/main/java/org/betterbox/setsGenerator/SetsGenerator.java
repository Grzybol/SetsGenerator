package org.betterbox.setsGenerator;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.betterbox.elasticBuffer.ElasticBuffer;
import org.betterbox.elasticBuffer.ElasticBufferAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.dsig.Transform;
import java.io.File;
import java.util.*;

public final class SetsGenerator extends JavaPlugin {
    private PluginLogger pluginLogger;
    FileManager fileManager;
    ConfigManager configManager;
    private String folderPath;
    private int chestplateBaseProtection;
    private int leggingsBaseProtection;
    private int helmetBaseProtection;
    private int bootsBaseProtection;
    private int chestplateProtectionPerLvL;
    private int leggingsProtectionPerLvL;
    private int bootsProtectionPerLvL;
    private int helmetProtectionPerLvL;
    private int swordDamage;
    private double talismanMovementSpeedBonus;
    private int talismanHealthBonus;
    private int chestplateHealthBonus;
    private int leggingHealthBonus;
    private int bootsHealthBonus;
    private int helmetHealthBonus;
    private String startColor;
    private String endColor;
    private String endColorIncreasePerLevel;
    private String startColorIncreasePerLevel,startColorIncreasePer10Levels;
    private ItemFactory itemFactory;
    private GuiManager guiManager;
    private Map<ItemStack,Integer> upgradeItems= new HashMap<>();
    private Map<Integer,Map<ItemStack,Integer>> upgradeLists= new HashMap<>();;
    //private final String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
    private int loadedLevels;
    private Placeholders placeholdersManager;
    private final NamespacedKey key = new NamespacedKey(this, "SetsGeneratorShop");

    @Override
    public void onEnable() {

        // Plugin startup logic
        folderPath = getDataFolder().getAbsolutePath();
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
        pluginLogger = new PluginLogger(folderPath, defaultLogLevels,this);
        fileManager = new FileManager(getDataFolder().getAbsolutePath(),this,this,pluginLogger);
        configManager = new ConfigManager(this, pluginLogger, folderPath,this);
        loadElasticBuffer();
        new CommandManager(this, configManager,pluginLogger);
        itemFactory = new ItemFactory(this,this,pluginLogger);
        guiManager = new GuiManager(this, itemFactory,pluginLogger);
        getServer().getPluginManager().registerEvents(new EventManager(this,guiManager,itemFactory,this,pluginLogger,fileManager,configManager), this);
        placeholdersManager = new Placeholders(this,configManager);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean success = placeholdersManager.register();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "SetsGenerator: Placeholders zostały zarejestrowane w PlaceholderAPI. success="+success);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "SetsGenerator: Warning: PlaceholderAPI not found, placeholders will NOT be available.");
        }
        // Opóźnienie wykonania o 1 sekundę (20 tików)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            String transactionID = UUID.randomUUID().toString();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing villagers by UUIDs",transactionID);
            List<UUID> villagerUUIDs = configManager.getVillagerUUIDs();
            for (UUID uuid : villagerUUIDs) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing villager with UUID: " + uuid,transactionID);
                Entity entity = Bukkit.getEntity(uuid);
                if (entity instanceof Villager) {
                    Villager villager = (Villager) entity;
                    // Ponowne ustawienie właściwości NPC
                    villager.setInvulnerable(true);
                    villager.setCollidable(false);
                    villager.setAI(true); // Upewnij się, że AI jest ustawione na true
                } else {
                    // Jeśli encja nie istnieje lub nie jest Villagerem
                    pluginLogger.log(PluginLogger.LogLevel.WARNING, "Villager not found for UUID: " + uuid+", entity: "+entity,transactionID);
                }
            }
            for (World world : Bukkit.getWorlds()) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing villagers in world: " + world.getName(),transactionID);
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Villager) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing villager: " + entity.getUniqueId()+", persistentDataContainer: "+ Arrays.toString(entity.getPersistentDataContainer().getKeys().toArray()),transactionID);
                        Villager villager = (Villager) entity;

                        if (villager.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Setting up villager: " + villager.getUniqueId(),transactionID);
                            configManager.setupVillager(villager,key,transactionID);
                        }
                    }
                }
            }
        }, 100L);// Opóźnienie o 20 tików (1 sekunda)
    }
    public NamespacedKey getKey() {
        return key;
    }
    private final List<String> tags = new ArrayList<>(Arrays.asList(
            "chestplate_level",
            "leggings_level",
            "helmet_level",
            "boots_level",
            "talisman_level",
            "sword_level"
    ));

    private void loadElasticBuffer(){
        try{
            PluginManager pm = Bukkit.getPluginManager();
            try {
                // Opóźnienie o 5 sekund, aby dać ElasticBuffer czas na pełną inicjalizację
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "[BetterElo] Initialization delay interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Przywrócenie statusu przerwania wątku
            }
            ElasticBuffer elasticBuffer = (ElasticBuffer) pm.getPlugin("ElasticBuffer");
            pluginLogger.isElasticBufferEnabled=true;
            pluginLogger.api= new ElasticBufferAPI(elasticBuffer);
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "ElasticBufferAPI instance found via ServicesManager, exception: "+e.getMessage());
        }
    }







    //GETTERS SETTERS
    public int getChestplateBaseProtection() {
        return chestplateBaseProtection;
    }

    public void setChestplateBaseProtection(int chestplateBaseProtection) {
        this.chestplateBaseProtection = chestplateBaseProtection;
    }

    public int getLeggingsBaseProtection() {
        return leggingsBaseProtection;
    }

    public void setLeggingsBaseProtection(int leggingsBaseProtection) {
        this.leggingsBaseProtection = leggingsBaseProtection;
    }

    public int getHelmetBaseProtection() {
        return helmetBaseProtection;
    }

    public void setHelmetBaseProtection(int helmetBaseProtection) {
        this.helmetBaseProtection = helmetBaseProtection;
    }

    public int getBootsBaseProtection() {
        return bootsBaseProtection;
    }

    public void setBootsBaseProtection(int bootsBaseProtection) {
        this.bootsBaseProtection = bootsBaseProtection;
    }

    public int getChestplateProtectionPerLvL() {
        return chestplateProtectionPerLvL;
    }

    public void setChestplateProtectionPerLvL(int chestplateProtectionPerLvL) {
        this.chestplateProtectionPerLvL = chestplateProtectionPerLvL;
    }

    public int getLeggingsProtectionPerLvL() {
        return leggingsProtectionPerLvL;
    }

    public void setLeggingsProtectionPerLvL(int leggingsProtectionPerLvL) {
        this.leggingsProtectionPerLvL = leggingsProtectionPerLvL;
    }

    public int getBootsProtectionPerLvL() {
        return bootsProtectionPerLvL;
    }

    public void setBootsProtectionPerLvL(int bootsProtectionPerLvL) {
        this.bootsProtectionPerLvL = bootsProtectionPerLvL;
    }

    public int getHelmetProtectionPerLvL() {
        return helmetProtectionPerLvL;
    }

    public void setHelmetProtectionPerLvL(int helmetProtectionPerLvL) {
        this.helmetProtectionPerLvL = helmetProtectionPerLvL;
    }

    public int getSwordDamage() {
        return swordDamage;
    }

    public void setSwordDamage(int swordDamage) {
        this.swordDamage = swordDamage;
    }

    public double getTalismanMovementSpeedBonus() {
        return talismanMovementSpeedBonus;
    }

    public void setTalismanMovementSpeedBonus(double talismanMovementSpeedBonus) {
        this.talismanMovementSpeedBonus = talismanMovementSpeedBonus;
    }

    public int getTalismanHealthBonus() {
        return talismanHealthBonus;
    }

    public void setTalismanHealthBonus(int talismanHealthBonus) {
        this.talismanHealthBonus = talismanHealthBonus;
    }

    public int getChestplateHealthBonus() {
        return chestplateHealthBonus;
    }

    public void setChestplateHealthBonus(int chestplateHealthBonus) {
        this.chestplateHealthBonus = chestplateHealthBonus;
    }

    public int getLeggingHealthBonus() {
        return leggingHealthBonus;
    }

    public void setLeggingHealthBonus(int leggingHealthBonus) {
        this.leggingHealthBonus = leggingHealthBonus;
    }

    public int getBootsHealthBonus() {
        return bootsHealthBonus;
    }

    public void setBootsHealthBonus(int bootsHealthBonus) {
        this.bootsHealthBonus = bootsHealthBonus;
    }

    public int getHelmetHealthBonus() {
        return helmetHealthBonus;
    }

    public void setHelmetHealthBonus(int helmetHealthBonus) {
        this.helmetHealthBonus = helmetHealthBonus;
    }
    public String getStartColor() {
        return startColor;
    }

    public void setStartColor(String startColor) {
        this.startColor = startColor;
    }

    public String getEndColor() {
        return endColor;
    }

    public void setEndColor(String endColor) {
        this.endColor = endColor;
    }
    public String getEndColorIncreasePerLevel() {
        return endColorIncreasePerLevel;
    }
    public void setUpgradeLists(Map<Integer,Map<ItemStack,Integer>> upgradeLists){
        this.upgradeLists=upgradeLists;
    }
    public Map<Integer,Map<ItemStack,Integer>> getUpgradeLists(){
        return upgradeLists;
    }

    public void setEndColorIncreasePerLevel(String endColorIncreasePerLevel) {
        this.endColorIncreasePerLevel = endColorIncreasePerLevel;
    }
    public String getStartColorIncreasePer10Levels() {
        return startColorIncreasePer10Levels;
    }


    public void setStartColorIncreasePer10Levels(String startColorIncreasePer10Levels) {
        this.startColorIncreasePer10Levels = startColorIncreasePer10Levels;
    }
    public String getStartColorIncreasePerLevel() {
        return startColorIncreasePerLevel;
    }
    public int getLoadedLevels() {
        return loadedLevels;
    }

    public void setLoadedLevels(int loadedLevels) {
        this.loadedLevels = loadedLevels;
    }

    public void setStartColorIncreasePerLevel(String startColorIncreasePerLevel) {
        this.startColorIncreasePerLevel = startColorIncreasePerLevel;
    }
    public String applyGradientv2(String text, String startColor, String endColor, int level) {
        // Konwersja stringów kolorów na tablice intów
        int[] startRGB = Arrays.stream(startColor.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endRGB = Arrays.stream(endColor.split(",")).mapToInt(Integer::parseInt).toArray();

        // Pobranie wartości zwiększania kolorów na poziom
        int[] startIncrease = Arrays.stream(getStartColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();
        int[] startIncrease10lvl = Arrays.stream(getStartColorIncreasePer10Levels().split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endIncrease = Arrays.stream(getEndColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();

        if (level > 1) {
            // Obliczamy ile pełnych bloków 10-poziomowych mamy
            int fullSets = level / 10;     // ile razy przekraczamy pełne 10 poziomów
            int leftover = level % 10;     // ile poziomów powyżej ostatniego pełnego bloku 10-poziomowego

            // Najpierw zastosujmy pełne 10-poziomowe bloki
            // Każdy pełny blok (np. poziomy 1–10, 11–20, ...) daje:
            // 8 x startIncrease + 1 x startIncrease10lvl na końcu bloku

            for (int i = 0; i < 3; i++) {
                // Dla każdego pełnego bloku: 8 razy startIncrease
                startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + startIncrease[i] * (fullSets * 8)));
                // Dla każdego pełnego bloku: 1 raz startIncrease10lvl
                startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + startIncrease10lvl[i] * fullSets));
            }

            // Teraz reszta (po ostatnim pełnym bloku 10 poziomów)
            // leftover razy zwiększamy za pomocą startIncrease
            if (leftover > 0) {
                for (int i = 0; i < 3; i++) {
                    startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + startIncrease[i] * leftover));
                }
            }

            // Teraz endRGB - tylko co 10 poziomów (jak poprzednio)
            int endMultiplier = (level - 1) / 10;
            for (int i = 0; i < 3; i++) {
                endRGB[i] = Math.min(255, Math.max(0, endRGB[i] + endIncrease[i] * endMultiplier));
            }
        }

        // Przygotowanie bufora na pokolorowany tekst
        StringBuilder coloredText = new StringBuilder();
        int textLength = text.length();

        // Stosowanie gradientu
        for (int i = 0; i < textLength; i++) {
            float ratio = (textLength > 1) ? (float) i / (textLength - 1) : 0;
            int red = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int green = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int blue = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            // Konwersja koloru RGB na string w formacie §x§r§g§b dla Minecrafta
            String colorCode = String.format("§x§%X§%X§%X§%X§%X§%X§l",
                    (red >> 4) & 0xF, red & 0xF,
                    (green >> 4) & 0xF, green & 0xF,
                    (blue >> 4) & 0xF, blue & 0xF);

            coloredText.append(colorCode).append(text.charAt(i));
        }

        return coloredText.toString();
    }

    public String applyGradient(String text, String startColor, String endColor, int level) {
        // Konwersja stringów kolorów na tablice intów
        int[] startRGB = Arrays.stream(startColor.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endRGB = Arrays.stream(endColor.split(",")).mapToInt(Integer::parseInt).toArray();

        // Pobranie wartości zwiększania kolorów na poziom
        int[] startIncrease = Arrays.stream(getStartColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();
        //int[] startIncrease10lvl = Arrays.stream(getStartColorIncreasePer10Levels().split(",")).mapToInt(Integer::parseInt).toArray();

        int[] endIncrease = Arrays.stream(getEndColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();
        int increments = level % 10;
        if (increments == 0 && level > 0) {
            increments = 10;
        }
        // Jeśli level > 1, modyfikujemy kolory początkowe i końcowe
        if (level > 1) {
            for (int i = 0; i < 3; i++) {
                startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + startIncrease[i] * increments));
            }
            // Dla endRGB tylko raz na 10 poziomów
            //int endMultiplier = (level) / 10; // ilość razy, ile należy zastosować endIncrease
            for (int i = 0; i < 3; i++) {
                endRGB[i] = Math.min(255, Math.max(0, endRGB[i] + endIncrease[i] * level));
            }
        }

        // Przygotowanie bufora na pokolorowany tekst
        StringBuilder coloredText = new StringBuilder();

        // Długość tekstu
        int textLength = text.length();
        //coloredText.append("§l"); // Włącz pogrubienie dla całego tekstu
        // Stosowanie gradientu
        for (int i = 0; i < textLength; i++) {
            // Obliczenie aktualnego koloru w gradientzie
            float ratio = (float) i / (textLength - 1);
            int red = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int green = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int blue = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            // Konwersja koloru RGB na string w formacie §x§r§g§b dla Minecrafta
            String colorCode = String.format("§x§%X§%X§%X§%X§%X§%X§l",
                    (red >> 4) & 0xF, red & 0xF,
                    (green >> 4) & 0xF, green & 0xF,
                    (blue >> 4) & 0xF, blue & 0xF);
            // Dodawanie pokolorowanego i pogrubionego znaku do wynikowego tekstu
            //coloredText.append(colorCode).append("§l").append(text.charAt(i));

            // Dodawanie pokolorowanego znaku do wynikowego tekstu
            coloredText.append(colorCode).append(text.charAt(i));
        }

        return coloredText.toString();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    /*
    public Map<String, Integer> getPlayerEquipmentLevels(Player player) {
        // Mapa wynikowa przechowująca najwyższy poziom każdego typu przedmiotu
        Map<String, Integer> equipmentLevels = new HashMap<>();

        // Klucze dla PersistentDataContainer
        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};

        // Przeglądaj ekwipunek gracza (inventory i założone przedmioty)
        List<ItemStack> allItems = new ArrayList<>();
        allItems.addAll(Arrays.asList(player.getInventory().getContents())); // Ekwipunek
        allItems.addAll(Arrays.asList(player.getInventory().getArmorContents())); // Założony pancerz

        for (ItemStack item : allItems) {
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();

            for (String tag : tags) {
                if (meta.getPersistentDataContainer().has(new NamespacedKey(this, tag), PersistentDataType.INTEGER)) {
                    int level = meta.getPersistentDataContainer().get(new NamespacedKey(this, tag), PersistentDataType.INTEGER);

                    // Aktualizuj wartość w mapie tylko, jeśli nowy poziom jest wyższy
                    equipmentLevels.put(tag, Math.max(equipmentLevels.getOrDefault(tag, 0), level));
                }
            }
        }

        return equipmentLevels;
    }
    public int getItemLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0; // Brak metadanych lub pusty przedmiot
        }

        ItemMeta meta = item.getItemMeta();
        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};

        for (String tag : tags) {
            NamespacedKey key = new NamespacedKey(this, tag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            }
        }

        return 0; // Brak odpowiedniego tagu
    }

     */
    public Map<String, Integer> getPlayerEquipmentLevels(Player player) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Starting getPlayerEquipmentLevels for player: " + player.getName());

        // Mapa wynikowa przechowująca najwyższy poziom każdego typu przedmiotu
        Map<String, Integer> equipmentLevels = new HashMap<>();

        // Klucze dla PersistentDataContainer
        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Initialized tags: " + Arrays.toString(tags));

        // Przeglądaj ekwipunek gracza (inventory i założone przedmioty)
        List<ItemStack> allItems = new ArrayList<>();
        allItems.addAll(Arrays.asList(player.getInventory().getContents())); // Ekwipunek
        allItems.addAll(Arrays.asList(player.getInventory().getArmorContents())); // Założony pancerz
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Collected all items from inventory and armor.");

        for (ItemStack item : allItems) {
            if (item == null) {
                //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Skipping null item.");
                continue;
            }
            if (!item.hasItemMeta()) {
                //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Skipping item without metadata: " + item.toString());
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing item with meta: " + item.toString());

            for (String tag : tags) {
                NamespacedKey key = new NamespacedKey(this, tag);
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Found tag '" + tag + "' with level " + level + " in item: " + item.toString());

                    // Aktualizuj wartość w mapie tylko, jeśli nowy poziom jest wyższy
                    int previousLevel = equipmentLevels.getOrDefault(tag, 0);
                    if (level > previousLevel) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Updating level for tag '" + tag + "' from " + previousLevel + " to " + level);
                    }
                    equipmentLevels.put(tag, Math.max(previousLevel, level));
                } else {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Tag '" + tag + "' not found in item.");
                }
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Final equipment levels: " + equipmentLevels.toString());
        return equipmentLevels;
    }
    public String getPlayerEquipmentSummary(Player player) {
        if (player == null) {
            return "Player not found";
        }

        // Pobieranie poziomów z poszczególnych części ekwipunku
        int helmetLevel = getHelmetLevel(player);
        int chestplateLevel = getChestplateLevel(player);
        int leggingsLevel = getLeggingsLevel(player);
        int bootsLevel = getBootsLevel(player);
        int talismanLevel = getTalismanLevel(player);
        int swordLevel = getSwordLevel(player);
        double avgLevel = getAverageEquipmentLevel(player);
        double maxHealth = player.getMaxHealth(); // Założenie, że jest taka metoda w API

        // Formatowanie stringa z poziomami i ikonami
        return String.format("❤ %s | ⭐ %.2f | 🗡 %d | ⛨ %d/%d/%d/%d | ☯ %d",
                maxHealth,
                avgLevel,
                swordLevel,
                helmetLevel,
                chestplateLevel,
                leggingsLevel,
                bootsLevel,
                talismanLevel);
    }

    public int getHelmetLevel(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        return getItemLevel(helmet, "helmet_level");
    }

    public int getChestplateLevel(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        return getItemLevel(chestplate, "chestplate_level");
    }

    public int getLeggingsLevel(Player player) {
        ItemStack leggings = player.getInventory().getLeggings();
        return getItemLevel(leggings, "leggings_level");
    }

    public int getBootsLevel(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        return getItemLevel(boots, "boots_level");
    }

    public int getTalismanLevel(Player player) {
        // Przykład, jak można by szukać talizmanu w ekwipunku
        ItemStack talisman = findItemInInventory(player, "talisman");
        return getItemLevel(talisman, "talisman_level");
    }

    public int getSwordLevel(Player player) {
        // Przykład, jak można by szukać miecza w ekwipunku
        ItemStack sword = findItemInInventory(player, "sword");
        return getItemLevel(sword, "sword_level");
    }

    private int getItemLevel(ItemStack item, String tag) {
        if (item == null || !item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(this, tag);
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        }
        return 0;
    }

    private ItemStack findItemInInventory(Player player, String itemType) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().toString().toLowerCase().contains(itemType.toLowerCase())) {
                return item;
            }
        }
        return null;
    }
    public double getAverageEquipmentLevel(Player player) {
        if (player == null) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Player object is null.");
            return 0.0;
        }
        pluginLogger.log(PluginLogger.LogLevel.PLACEHOLDER, "Starting getAverageEquipmentLevel for player: " + player.getName());

        // Klucze dla PersistentDataContainer
        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Initialized tags: " + Arrays.toString(tags));

        // Lista, która będzie przechowywać poziomy wszystkich istniejących tagów
        List<Integer> levels = new ArrayList<>();

        // Przeglądaj ekwipunek gracza (inventory i założone przedmioty)
        List<ItemStack> allItems = new ArrayList<>();
        allItems.addAll(Arrays.asList(player.getInventory().getContents())); // Ekwipunek
        allItems.addAll(Arrays.asList(player.getInventory().getArmorContents())); // Założony pancerz
        pluginLogger.log(PluginLogger.LogLevel.PLACEHOLDER, "Collected all items from inventory and armor.");

        for (ItemStack item : allItems) {
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            for (String tag : tags) {
                NamespacedKey key = new NamespacedKey(this, tag);
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    levels.add(level);
                    pluginLogger.log(PluginLogger.LogLevel.PLACEHOLDER, "Added level " + level + " for tag '" + tag + "' from item: " + item.toString());
                }
            }
        }

        if (levels.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.PLACEHOLDER, "No levels found, returning average as 0.");
            return 0.0;
        }

        double average = levels.stream().mapToInt(Integer::intValue).average().getAsDouble();
        pluginLogger.log(PluginLogger.LogLevel.PLACEHOLDER, "Calculated average level: " + average);
        return average;
    }

    public int getItemSlotFromPlayerEqByTag(Player player, String tag,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Starting getItemSlotFromPlayerEqByTag for player: " + player.getName() + " and tag: " + tag,transactionID);

        // Usuwamy wszystko poza podstawową nazwą tagu
        String cleanTag = tag.split(":")[0].trim(); // Oczyszczenie tagu z dodatkowych informacji i spacji
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "getItemSlotFromPlayerEqByTag Using clean tag: " + cleanTag,transactionID);

        // Przechodzi przez wszystkie przedmioty w ekwipunku gracza
        ItemStack[] inventoryItems = player.getInventory().getContents();
        for (int i = 0; i < inventoryItems.length; i++) {
            ItemStack item = inventoryItems[i];
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(this, cleanTag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Found clean tag '" + cleanTag + "' in item at slot " + i,transactionID);
                return i; // Zwraca slot, w którym znajduje się przedmiot
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Clean tag '" + cleanTag + "' not found in any items.",transactionID);
        return -1; // Jeśli tag nie zostanie znaleziony, zwróć -1
    }
    public ItemStack getItemFromPlayerEqByTag(Player player, String tag,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Starting getItemFromPlayerEqByTag for player: " + player.getName() + " and tag: " + tag);
        String cleanTag = tag.split(":")[0].trim(); // Oczyszczenie tagu z dodatkowych informacji i spacji
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "getItemFromPlayerEqByTag Using clean tag: " + cleanTag);
        // Przechodzi przez wszystkie przedmioty w ekwipunku gracza
        ItemStack[] inventoryItems = player.getInventory().getContents();
        for (ItemStack item : inventoryItems) {
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(this, cleanTag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Found cleanTag '" + cleanTag + "' in item: " + item.toString());
                return item; // Zwraca przedmiot zawierający tag
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "cleanTag '" + cleanTag + "' not found in any items.");
        return null; // Jeśli cleanTag nie zostanie znaleziony, zwróć null
    }
    public ItemStack getItemFromPlayerSlot(Player player, int slot) {
        if (slot < 0 || slot >= player.getInventory().getSize()) {
            // Jeśli slot jest poza zakresem ekwipunku, zwróć null
            return null;
        }
        return player.getInventory().getItem(slot); // Zwraca przedmiot w określonym slocie
    }
    public Map<ItemStack, Integer> getUpgradeItems(int level) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Fetching upgrade items for level: " + level);

        // Pobierz mapę przedmiotów dla danego poziomu
        Map<ItemStack, Integer> itemsForLevel = upgradeLists.get(level);

        // Sprawdź, czy istnieje wpis dla tego poziomu
        if (itemsForLevel != null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Found items for level " + level);
            // Zwróć kopię mapy, aby uniknąć modyfikacji oryginalnych danych przez użytkownika klasy
            return new HashMap<>(itemsForLevel);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "No items found for level " + level + ", returning empty map");
            // Jeśli nie ma wpisu dla tego poziomu, zwróć pustą mapę
            return new HashMap<>();
        }
    }
    public boolean hasRequiredItems(Player player, Map<ItemStack, Integer> requiredItems,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Checking if player " + player.getName() + " has all required items",transactionID);
        if (requiredItems == null || requiredItems.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Required items map is empty. No items to check for player " + player.getName(),transactionID);
            return true; // Brak wymaganych przedmiotów
        }
        Inventory inventory = player.getInventory();

        // Sprawdź, czy gracz posiada wszystkie wymagane przedmioty w odpowiednich ilościach
        for (Map.Entry<ItemStack, Integer> entry : requiredItems.entrySet()) {
            ItemStack requiredItem = entry.getKey();
            int requiredAmount = entry.getValue();

            if (!inventory.containsAtLeast(requiredItem, requiredAmount)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Player " + player.getName() + " does not have enough of: " + requiredItem,transactionID);
                return false; // Brak wystarczającej ilości danego przedmiotu
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Player " + player.getName() + " has all required items.",transactionID);
        return true;
    }
    public void upgradeItem(Player player, String tag, int newLevel, String transactionID){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "SetsGenerator.upgradeItem called. player: "+player.getName()+", tag:"+tag+", newLevel: "+newLevel);
        ItemStack itemToUpgrade = getItemFromPlayerEqByTag(player,tag,transactionID);
        int slot = getItemSlotFromPlayerEqByTag(player,tag,transactionID);
        ItemStack newItem = null;
        switch (itemToUpgrade.getType()) {
            case LEATHER_HELMET:
                newItem = itemFactory.createHelmet(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new helmet with level " + newLevel,transactionID);
                break;
            case DIAMOND_SWORD:
                newItem = itemFactory.createSword(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new sword with level " + newLevel,transactionID);
                break;
            case MAGMA_CREAM:
                newItem = itemFactory.createTalisman(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new talisman with level " + newLevel,transactionID);
                break;
            case LEATHER_LEGGINGS:
                newItem = itemFactory.createLeggings(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new leggings with level " + newLevel,transactionID);
                break;
            case ELYTRA:
                newItem = itemFactory.createChestplate(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new chestplate with level " + newLevel,transactionID);
                break;
            case LEATHER_BOOTS:
                newItem = itemFactory.createBoots(newLevel);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Created new boots with level " + newLevel,transactionID);
                break;
            default:
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: This item type cannot be upgraded.",transactionID);
                player.sendMessage(ChatColor.RED + "This item cannot be upgraded.");
                return;
        }

        if (newItem == null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: newItem is null, upgrade failed.");
            player.sendMessage(ChatColor.RED + "Failed to create upgraded item.");
            return;
        }
        // Ustawiamy nowy przedmiot w slocie gracza
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Setting upgraded item in player's inventory at slot " + slot,transactionID);
        player.getInventory().setItem(slot, newItem);

        // Opcjonalnie można wymusić aktualizację ekwipunku (w niektórych przypadkach pomocne)
        player.updateInventory();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Player inventory updated.",transactionID);
        // Dodatkowa weryfikacja
        ItemStack checkItem = player.getInventory().getItem(slot);
        if (checkItem != null && checkItem.equals(newItem)) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "EventManager.onPlayerClick: Upgrade successful. New item is now in the slot.",transactionID);
            fileManager.updatePlayerEquipmentLevel(player.getUniqueId(),tag,newLevel, transactionID);
        } else {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "EventManager.onPlayerClick: Upgrade attempt did not reflect in player's inventory. Check itemFactory and item creation methods.",transactionID);
        }
    }
    public boolean checkAndRemoveItems(Player player, Map<ItemStack, Integer> requiredItems,String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Checking if player " + player.getName() + " has all required items. requiredItems: "+requiredItems,transactionID);
        if (requiredItems == null || requiredItems.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Required items map is empty. No items to check or remove for player " + player.getName(), transactionID);
            return true;
        }
        Inventory inventory = player.getInventory();

        // Sprawdzenie posiadanych przedmiotów
        for (Map.Entry<ItemStack, Integer> entry : requiredItems.entrySet()) {
            ItemStack requiredItem = entry.getKey();
            int requiredAmount = entry.getValue();

            if (!inventory.containsAtLeast(requiredItem, requiredAmount)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Player " + player.getName() + " does not have enough of: " + requiredItem,transactionID);
                return false;
            }
        }

        // Usuwanie przedmiotów - klonujemy wymagany przedmiot i ustawiamy jego ilość
        for (Map.Entry<ItemStack, Integer> entry : requiredItems.entrySet()) {
            ItemStack requiredItem = entry.getKey().clone(); // klonujemy, by zachować metadane
            int requiredAmount = entry.getValue();
            requiredItem.setAmount(requiredAmount);

            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Removing " + requiredAmount + " of " + requiredItem + " from player " + player.getName(),transactionID);
            inventory.removeItem(requiredItem);
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "All required items removed for player " + player.getName(),transactionID);
        return true;
    }

    public int getItemLevel(ItemStack item) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Starting getItemLevel for item: " + (item != null ? item.toString() : "null"));

        if (item == null) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Item is null, returning level 0.");
            return 0;
        }
        if (!item.hasItemMeta()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Item has no metadata, returning level 0.");
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Processing item meta: " + meta.toString());

        //String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Initialized tags: " + Arrays.toString(tags.toArray()));

        for (String tag : tags) {
            NamespacedKey key = new NamespacedKey(this, tag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                int level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Found tag '" + tag + "' with level " + level);
                return level;
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Tag '" + tag + "' not found.");
            }
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "No tags found, returning level 0.");
        return 0;
    }
    public boolean isFromUpgradeGUI(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(this, "isForUpgradeGUI");

        return meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }
    public boolean hasAnyTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        tags.add("isForUpgradeGUI");
        tags.add("Confirm");
        tags.add("Cancel");

        for (String tag : tags) {
            NamespacedKey key = new NamespacedKey(this, tag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                return true;
            }
        }

        return false;
    }
    public String getTagFromItemstack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null; // Brak przedmiotu lub metadanych
        }

        ItemMeta meta = item.getItemMeta();
        String[] tags = {"chestplate_level", "leggings_level", "helmet_level", "boots_level", "talisman_level", "sword_level"};

        for (String tag : tags) {
            NamespacedKey key = new NamespacedKey(this, tag);
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                int value = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                return tag; // Zwraca nazwę tagu i jego wartość
            }
        }

        return null; // Żaden z tagów nie został znaleziony
    }
    public ItemFactory getItemFactory(){
        return itemFactory;
    }
    public FileManager getFileManager(){
        return fileManager;
    }
    public GuiManager getGuiManager(){return guiManager;}
    public List<String> getItemNamesWithQuantity(Map<ItemStack, Integer> items) {
        List<String> result = new ArrayList<>();
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            ItemStack item = entry.getKey();
            int quantity = entry.getValue();

            String name;
            if (item.hasItemMeta() && item.getItemMeta().displayName() != null) {
                name = plainSerializer.serialize(item.getItemMeta().displayName());
            } else {
                name = item.getType().name().replace("_", " ").toLowerCase();
            }

            name += " x" + quantity;
            result.add(name);
        }

        return result;
    }
}
