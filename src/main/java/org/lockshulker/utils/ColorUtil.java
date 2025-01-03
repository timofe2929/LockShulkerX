package org.lockshulker.utils;

import org.bukkit.ChatColor;

public class ColorUtil {

    public static String colorize(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(message);
    }

    public static ChatColor getColor(String color) {
        if (color == null || color.isEmpty()) {
            return ChatColor.WHITE;
        }
        try {
            return ChatColor.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }
}