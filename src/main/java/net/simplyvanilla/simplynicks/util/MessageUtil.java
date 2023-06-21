package net.simplyvanilla.simplynicks.util;

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.simplyvanilla.simplynicks.SimplyNicks;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    private MessageUtil() {
    }

    public static void sendMessage(CommandSender commandSender, String message) {
        sendMessage(commandSender, message, new HashMap<>());
    }

    public static void sendMessage(CommandSender commandSender, String key,
                                   Map<String, String> replacements) {
        String message = SimplyNicks.getInstance().getConfig().getString(key);
        if (message == null) {
            message = key;
        }

        commandSender.sendMessage(MiniMessage.miniMessage()
            .deserialize(message, replacements.entrySet().stream()
                .map(entry -> {
                    if (entry.getValue().contains("&")) {
                        return Placeholder.component(entry.getKey(),
                            LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(entry.getValue()));
                    }

                    return Placeholder.unparsed(entry.getKey(),
                        entry.getValue());
                }).toList().toArray(TagResolver[]::new)));
    }
}
