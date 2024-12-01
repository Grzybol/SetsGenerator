package org.betterbox.setsGenerator;

import com.google.common.collect.Sets;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemFactory {
    private JavaPlugin plugin;
    private SetsGenerator setsGenerator;

    public ItemFactory(JavaPlugin plugin, SetsGenerator setsGenerator) {
        this.setsGenerator=setsGenerator;
        this.plugin = plugin;
    }

    public ItemStack createSword(int level) {
        return createItem(Material.DIAMOND_SWORD, level, setsGenerator.applyGradient("Blade Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createSword() {
        return createSword(0); // Poziom podstawowy
    }

    public ItemStack createTalisman(int level) {
        return createItem(Material.MAGMA_CREAM, level, setsGenerator.applyGradient("Talisman Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createTalisman() {
        return createTalisman(0); // Poziom podstawowy
    }

    public ItemStack createLeggings(int level) {
        return createItem(Material.LEATHER_LEGGINGS, level, setsGenerator.applyGradient("Leggings Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createLeggings() {
        return createLeggings(0); // Poziom podstawowy
    }

    public ItemStack createChestplate(int level) {
        return createItem(Material.LEATHER_CHESTPLATE, level, setsGenerator.applyGradient("Chestplate Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createChestplate() {
        return createChestplate(0); // Poziom podstawowy
    }

    public ItemStack createBoots(int level) {
        return createItem(Material.LEATHER_BOOTS, level, setsGenerator.applyGradient("Boots Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createBoots() {
        return createBoots(0); // Poziom podstawowy
    }

    public ItemStack createHelmet(int level) {
        return createItem(Material.LEATHER_HELMET, level, setsGenerator.applyGradient("Helmet Tier", setsGenerator.getStartColor(), setsGenerator.getEndColor(),level));
    }

    public ItemStack createHelmet() {
        return createHelmet(0); // Poziom podstawowy
    }
    private ItemStack createItem(Material material, int level, String tag) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        // Ustawiamy nazwę przedmiotu
        meta.setDisplayName(tag + " ✪" + level);


        // Przygotowanie Lore i tagów
        List<String> lore = new ArrayList<>();
        if (material == Material.LEATHER_CHESTPLATE) {
            lore.add("    +" + (level * setsGenerator.getChestplateHealthBonus()) + " [❤]");
            lore.add("    +" + (level * setsGenerator.getChestplateProtectionPerLvL()) + " [⛨]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "chestplate_level"), PersistentDataType.INTEGER, level);
        } else if (material == Material.LEATHER_LEGGINGS) {
            lore.add("    +" + (level * setsGenerator.getLeggingHealthBonus()) + " [❤]");
            lore.add("    +" + (level * setsGenerator.getLeggingsProtectionPerLvL()) + " [⛨]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "leggings_level"), PersistentDataType.INTEGER, level);
        } else if (material == Material.LEATHER_HELMET) {
            lore.add("    +" + (level * setsGenerator.getHelmetHealthBonus()) + " [❤]");
            lore.add("    +" + (level * setsGenerator.getHelmetProtectionPerLvL()) + " [⛨]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "helmet_level"), PersistentDataType.INTEGER, level);
        } else if (material == Material.LEATHER_BOOTS) {
            lore.add("    +" + (level * setsGenerator.getBootsHealthBonus()) + " [❤]");
            lore.add("    +" + (level * setsGenerator.getBootsProtectionPerLvL()) + " [⛨]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "boots_level"), PersistentDataType.INTEGER, level);
        } else if (material == Material.MAGMA_CREAM) { // Talisman
            lore.add("    +" + (level * setsGenerator.getTalismanHealthBonus()) + " [❤]");
            lore.add("    +" + String.format("%.2f", level * setsGenerator.getTalismanMovementSpeedBonus()) + " [➟]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "talisman_level"), PersistentDataType.INTEGER, level);
        } else if (material == Material.DIAMOND_SWORD) {
            lore.add("    +" + (level * setsGenerator.getSwordDamage()) + " [🗡]");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "sword_level"), PersistentDataType.INTEGER, level);
        }

        // Ustawienie Lore w metadanych
        meta.setLore(lore);

        // Ustawianie koloru skóry dla skórzanej zbroi
        if (material == Material.LEATHER_CHESTPLATE || material == Material.LEATHER_LEGGINGS
                || material == Material.LEATHER_HELMET || material == Material.LEATHER_BOOTS) {
            setLeatherArmorColor((LeatherArmorMeta) meta, level);
        }

        // Nadawanie odpowiednich atrybutów
        if (material == Material.DIAMOND_CHESTPLATE) {
            addArmorAttributes(meta, EquipmentSlot.CHEST, level, setsGenerator.getChestplateBaseProtection(), setsGenerator.getChestplateHealthBonus());
        } else if (material == Material.DIAMOND_LEGGINGS) {
            addArmorAttributes(meta, EquipmentSlot.LEGS, level, setsGenerator.getLeggingsBaseProtection(), setsGenerator.getLeggingHealthBonus());
        } else if (material == Material.DIAMOND_HELMET) {
            addArmorAttributes(meta, EquipmentSlot.HEAD, level, setsGenerator.getHelmetBaseProtection(), setsGenerator.getHelmetHealthBonus());
        } else if (material == Material.DIAMOND_BOOTS) {
            addArmorAttributes(meta, EquipmentSlot.FEET, level, setsGenerator.getBootsBaseProtection(), setsGenerator.getBootsHealthBonus());
        } else if (material == Material.MAGMA_CREAM) { // Talisman
            AttributeModifier healthModifier = new AttributeModifier(
                    UUID.randomUUID(), "generic.maxHealth", level * setsGenerator.getTalismanHealthBonus(),
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, healthModifier);

            AttributeModifier speedModifier = new AttributeModifier(
                    UUID.randomUUID(), "generic.movementSpeed", level * setsGenerator.getTalismanMovementSpeedBonus(),
                    AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.OFF_HAND);
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, speedModifier);
        }

        if (material == Material.DIAMOND_SWORD) {
            AttributeModifier damageModifier = new AttributeModifier(
                    UUID.randomUUID(), "generic.attackDamage", level * setsGenerator.getSwordDamage(),
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        }

        // Przypisanie metadanych do przedmiotu
        item.setItemMeta(meta);
        return item;
    }
    private void setLeatherArmorColor(LeatherArmorMeta meta, int level) {
        int[] startRGB = Arrays.stream(setsGenerator.getStartColor().split(",")).mapToInt(Integer::parseInt).toArray();
        int[] increaseRGB = Arrays.stream(setsGenerator.getStartColorIncreasePerLevel().split(",")).mapToInt(Integer::parseInt).toArray();

        // Modyfikowanie koloru na podstawie poziomu
        if (level > 1) {
            for (int i = 0; i < 3; i++) {
                startRGB[i] = Math.min(255, Math.max(0, startRGB[i] + increaseRGB[i] * (level - 1)));
            }
        }

        // Ustawianie koloru na skórzanym pancerzu
        Color armorColor = Color.fromRGB(startRGB[0], startRGB[1], startRGB[2]);
        meta.setColor(armorColor);
    }

    private void addArmorAttributes(ItemMeta meta, EquipmentSlot slot, int level, int baseProtection, int healthBonus) {
        // Dodajemy atrybut zdrowia
        AttributeModifier healthModifier = new AttributeModifier(
                UUID.randomUUID(), "generic.maxHealth", level * healthBonus,
                AttributeModifier.Operation.ADD_NUMBER, slot);
        meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, healthModifier);

        // Dodajemy atrybut pancerza
        AttributeModifier armorModifier = new AttributeModifier(
                UUID.randomUUID(), "generic.armor", level * baseProtection,
                AttributeModifier.Operation.ADD_NUMBER, slot);
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorModifier);
    }
    private ItemStack createItemOld(Material material, int level, String tag) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tag + " ✪" + level);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "item_level"), PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
        return item;
    }

}
