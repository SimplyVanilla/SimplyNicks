package net.simplyvanilla.simplynicks.util;

import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    public static void sendMessage(CommandSender commandSender, String message) {
        sendMessage(commandSender, message, new HashMap<>());
    }

    public static void sendMessage(CommandSender commandSender, String message, Map<String, String> replacements) {
        message = SimplyNicks.getInstance().getConfig().getString(message);
        if (message != null) {
            for (var entry : replacements.entrySet()) {
                message = message.replace("[" + entry.getKey() + "]", entry.getValue());
            }
            message = ChatColor.translateAlternateColorCodes('&', message);

            commandSender.sendMessage(message);
        }
    }
}
