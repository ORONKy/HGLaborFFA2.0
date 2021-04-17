package de.hglabor.plugins.ffa.config;

import de.hglabor.plugins.ffa.FFA;
import de.hglabor.utils.localization.Localization;

import java.util.Locale;

public class FFAConfig {
    private FFAConfig() {
    }

    public static void load() {
        FFA.getPlugin().getConfig().addDefault("ffa.size", 100);
        FFA.getPlugin().getConfig().addDefault("ffa.duration", 1800);
        FFA.getPlugin().getConfig().addDefault("damage.sword.nerf", 0.65);
        FFA.getPlugin().getConfig().addDefault("damage.other.nerf", 0.2);
        FFA.getPlugin().getConfig().addDefault("border.skyborder.damage", 5);
        FFA.getPlugin().getConfig().addDefault("border.skyborder.height", 128);
        FFA.getPlugin().getConfig().options().copyDefaults(true);
        FFA.getPlugin().saveConfig();
    }

    public static int getInteger(String key) {
        return FFA.getPlugin().getConfig().getInt(key);
    }

    public static String getString(String key) {
        return FFA.getPlugin().getConfig().getString(key);
    }

    public static double getDouble(String key) {
        return FFA.getPlugin().getConfig().getDouble(key);
    }

    public static boolean getBoolean(String key) {
        return FFA.getPlugin().getConfig().getBoolean(key);
    }

    public static String getPrefix() {
        return Localization.INSTANCE.getMessage("hglabor.prefix", Locale.ENGLISH) + " ";
    }
}
