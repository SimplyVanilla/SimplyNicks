package net.simplyvanilla.simplynicks.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class NickValidationUtil {

    public enum ColorGroup {
        ALL,
        DEFAULT,
        NONE,
    }

    private static final Pattern PATTERN = Pattern.compile("\\A[A-Za-z0-9_]{3,16}\\Z");

    private static final String COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    public static ColorGroup getColorGroup(String nick, List<String> defaultColors) {
        char[] c = nick.toCharArray();
        boolean hasColor = false;

        for (int i = 0; i < c.length - 1; i++) {
            if (c[i] == '&' && COLOR_CODES.indexOf(c[i + 1]) > -1) {
                if (!defaultColors.contains(String.valueOf(c[i + 1]))) {
                    return ColorGroup.ALL;
                }
                hasColor = true;
            }
        }

        if (hasColor) {
            return ColorGroup.DEFAULT;
        } else {
            return ColorGroup.NONE;
        }
    }

    public static boolean isValidNick(String nick) {
        nick = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', nick));
        Matcher matcher = PATTERN.matcher(nick);
        return matcher.matches();
    }
}
