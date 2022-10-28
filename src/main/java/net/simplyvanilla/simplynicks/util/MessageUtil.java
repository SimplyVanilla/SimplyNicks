package net.simplyvanilla.simplynicks.util;

import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    public static void sendMessage(CommandSender commandSender, String message) {
        sendMessage(commandSender, message, "");
    }

    public static void sendMessage(CommandSender commandSender, String message, String nick) {
        message = SimplyNicks.getInstance().getConfig().getString(message);
        message = message.replace("[nick]", nick);
        message = ChatColor.translateAlternateColorCodes('&', message);

        commandSender.sendMessage(message);
    }
}
