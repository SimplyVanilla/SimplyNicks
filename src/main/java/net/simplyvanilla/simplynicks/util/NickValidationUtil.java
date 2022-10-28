package net.simplyvanilla.simplynicks.util;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickValidationUtil {

    public enum ColorGroup {
        DEFAULT,
        ALL
    }

    private static final Pattern PATTERN = Pattern.compile("\\A[A-Za-z0-9_]{3,16}\\Z");

    private static final String colorCodes = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    public static ColorGroup getColorGroup(String name, List<String> defaultColors) {
        char[] c = name.toCharArray();

        for (int i = 0; i < c.length - 1; i++) {
            if (c[i] == '&' && colorCodes.indexOf(c[i + 1]) > -1 && !defaultColors.contains(Arrays.toString(c))) {
                return ColorGroup.ALL;
            }
        }

        return ColorGroup.DEFAULT;
    }

    public static boolean isValidNick(String name, Boolean keepColorCodes) {
        if (!keepColorCodes) {
            name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        }

        Matcher matcher = PATTERN.matcher(name);

        return matcher.matches();
    }
}
