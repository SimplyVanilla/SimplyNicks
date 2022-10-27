// 
// Decompiled by Procyon v0.5.36
// 

package me.orbitium.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import me.clip.placeholderapi.PlaceholderAPI;
import java.util.UUID;
import me.orbitium.SimplyNicks;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.Listener;

public class PlayerEvents implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskLater((Plugin)SimplyNicks.getInstance(), () -> {

            if (!SimplyNicks.getCache().isNameAvailable(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName())) {
                OfflinePlayer fakeNamedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(SimplyNicks.getCache().getUUIDByName(event.getPlayer().getName())));
                if (fakeNamedPlayer.isOnline()) {
                    fakeNamedPlayer.getPlayer().setDisplayName(fakeNamedPlayer.getName());
                    fakeNamedPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(fakeNamedPlayer.getPlayer(), SimplyNicks.getInstance().getConfig().getString("messages.error.nickFixedByOwnerMessage"))));
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
