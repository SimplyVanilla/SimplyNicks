package net.simplyvanilla.simplynicks.event;

import java.util.UUID;
import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerEvents implements Listener {
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerLogin(PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskLater(SimplyNicks.getInstance(), () -> {
            if (!SimplyNicks.getCache().isNameAvailable(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName())) {
                OfflinePlayer fakeNamedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(SimplyNicks.getCache().getUUIDByName(event.getPlayer().getName())));
                if (fakeNamedPlayer.isOnline()) {
                    fakeNamedPlayer.getPlayer().setDisplayName(fakeNamedPlayer.getName());
                    MessageUtil.sendMessage(fakeNamedPlayer.getPlayer(), "messages.error.nickFixedByOwnerMessage");
                }

                SimplyNicks.getDatabase().updatePlayerNameData(fakeNamedPlayer.getUniqueId().toString(), fakeNamedPlayer.getName());
            }

            String name = SimplyNicks.getDatabase().getPlayerNameData(event.getPlayer().getUniqueId().toString());
            if (name == null) {
                name = event.getPlayer().getName();
            }

            SimplyNicks.getCache().addNewName(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName());
            event.getPlayer().setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            event.getPlayer().setCustomNameVisible(true);
        }, 1L);
    }
}
