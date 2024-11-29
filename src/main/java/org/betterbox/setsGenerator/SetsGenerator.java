package org.betterbox.setsGenerator;

import org.betterbox.elasticBuffer.ElasticBuffer;
import org.betterbox.elasticBuffer.ElasticBufferAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    private String startColorIncreasePerLevel;
    private ItemFactory itemFactory;
    private GuiManager guiManager;


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
        configManager = new ConfigManager(this, pluginLogger, folderPath,this);
        fileManager = new FileManager(getDataFolder().getAbsolutePath(),this,this,pluginLogger);
        configManager.ReloadConfig();
        loadElasticBuffer();
        new CommandManager(this, configManager);
        itemFactory = new ItemFactory(this,this);
        guiManager = new GuiManager(this, itemFactory);
        getServer().getPluginManager().registerEvents(new EventManager(this), this);

    }


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

    public void setEndColorIncreasePerLevel(String endColorIncreasePerLevel) {
        this.endColorIncreasePerLevel = endColorIncreasePerLevel;
    }

    public String getStartColorIncreasePerLevel() {
        return startColorIncreasePerLevel;
    }

    public void setStartColorIncreasePerLevel(String startColorIncreasePerLevel) {
        this.startColorIncreasePerLevel = startColorIncreasePerLevel;
    }

    public String applyGradient(String text, String startColor, String endColor, int level) {
        // Konwersja stringów kolorów na tablice intów
        int[] startRGB = Arrays.stream(startColor.split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endRGB = Arrays.stream(endColor.split(",")).mapToInt(Integer::parseInt).toArray();

        // Pobranie wartości zwiększania kolorów na poziom
        int[] startIncrease = Arrays.stream(getStartColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();
        int[] endIncrease = Arrays.stream(getEndColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();

        // Jeśli level > 1, modyfikujemy kolory początkowe i końcowe
        if (level > 1) {
            for (int i = 0; i < 3; i++) {
                startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + startIncrease[i] * (level - 1)));
                endRGB[i] = Math.min(255, Math.max(0, endRGB[i] + endIncrease[i] * (level - 1)));
            }
        }

        // Przygotowanie bufora na pokolorowany tekst
        StringBuilder coloredText = new StringBuilder();

        // Długość tekstu
        int textLength = text.length();

        // Stosowanie gradientu
        for (int i = 0; i < textLength; i++) {
            // Obliczenie aktualnego koloru w gradientzie
            float ratio = (float) i / (textLength - 1);
            int red = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int green = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int blue = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            // Konwersja koloru RGB na string w formacie §x§r§g§b dla Minecrafta
            String colorCode = String.format("§x§%X§%X§%X§%X§%X§%X",
                    (red >> 4) & 0xF, red & 0xF,
                    (green >> 4) & 0xF, green & 0xF,
                    (blue >> 4) & 0xF, blue & 0xF);

            // Dodawanie pokolorowanego znaku do wynikowego tekstu
            coloredText.append(colorCode).append(text.charAt(i));
        }

        return coloredText.toString();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
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
    public ItemFactory getItemFactory(){
        return itemFactory;
    }

}
