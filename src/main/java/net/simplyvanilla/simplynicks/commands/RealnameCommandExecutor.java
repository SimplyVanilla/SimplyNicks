package net.simplyvanilla.simplynicks.commands;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.NickValidationUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RealnameCommandExecutor implements CommandExecutor {
    private final SimplyNicks plugin;

    public RealnameCommandExecutor(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {

        Map<String, String> messageReplacements = new HashMap<>();

        if (args.length != 1) {
            return false;
        }

        if (!NickValidationUtil.isValidNick(args[0])) {
            return false;
        }

        messageReplacements.put("nick", args[0]);

        UUID uuid = this.plugin.getCache().getUUIDByNick(args[0]);
        if (uuid == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.nickNotFound");
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null) {
            this.plugin.sendConfigMessage(sender, "messages.error.playerCannotFoundErrorMessage");
            return true;
        }

        messageReplacements.put("realname", player.getName());

        this.plugin.sendConfigMessage(sender, "messages.realname", messageReplacements);

        return true;
    }
}
