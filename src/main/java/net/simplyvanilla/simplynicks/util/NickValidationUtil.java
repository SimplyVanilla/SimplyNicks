package net.simplyvanilla.simplynicks.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickValidationUtil {

    public enum ColorGroup {
        ALL,
        DEFAULT,
        NONE,
    }

    private static final Pattern PATTERN = Pattern.compile("\\A[A-Za-z0-9_]{3,16}\\Z");

    private static final String colorCodes = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    public static ColorGroup getColorGroup(String name, List<String> defaultColors) {
        char[] c = name.toCharArray();

        for (int i = 0; i < c.length - 1; i++) {
            if (c[i] == '&' && colorCodes.indexOf(c[i + 1]) > -1) {
                if (!defaultColors.contains(String.valueOf(c[i + 1]))) {
                    return ColorGroup.ALL;
                } else {
                    return ColorGroup.DEFAULT;
                }
            }
        }

        return ColorGroup.NONE;
    }

    public static boolean isValidNick(String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        Matcher matcher = PATTERN.matcher(name);
        return matcher.matches();
    }
}
