package org.betterbox.setsGenerator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
    private final SetsGenerator plugin;
    private final ConfigManager configManager;

    public Placeholders(SetsGenerator plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean canRegister() {
        return plugin.isEnabled();
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String getIdentifier() {
        return "sg"; // Prefix Twoich placeholderów, np. %betterquests_placeholder%
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    public String formatTime(long milliseconds) {
        if (milliseconds < 0) {
            return "Time is up!";
        }

        long totalSeconds = milliseconds / 1000;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder timeString = new StringBuilder();

        if (days > 0) {
            timeString.append(days).append(" ").append("days").append(" | ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append("hours").append(" | ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append("minutes").append(" | ");
        }
        if (seconds > 0 || timeString.length() == 0) {
            timeString.append(seconds).append(" ").append("seconds");
        }

        return timeString.toString().trim();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player != null) {
        }
        //double points=0;
        switch (identifier) {
            case "avg_level":
                return String.valueOf(plugin.getAverageEquipmentLevel(player));
            case "helmet_level":
                return String.valueOf(plugin.getHelmetLevel(player));
            case "chestplate_level":
                return String.valueOf(plugin.getChestplateLevel(player));
            case "leggings_level":
                return String.valueOf(plugin.getLeggingsLevel(player));
            case "boots_level":
                return String.valueOf(plugin.getBootsLevel(player));
            case "talisman_level":
                return String.valueOf(plugin.getTalismanLevel(player));
            case "sword_level":
                return String.valueOf(plugin.getSwordLevel(player));
            case "player_summary":
                return String.valueOf(plugin.getPlayerEquipmentSummary(player));
        }
        return null; // Zwróć null, jeśli placeholder nie jest obsługiwany
    }
}
