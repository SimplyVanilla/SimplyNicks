package net.simplyvanilla.simplynicks.event;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class PlayerEvents implements Listener {
    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onPlayerLogin(PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskLater(SimplyNicks.getInstance(), () -> {

            // check if logging in player-name is clashing with existing nick -> prefer player-name over nick
            if (!SimplyNicks.getCache().isNickAvailable(event.getPlayer().getName())) {

                String matchedUUID = SimplyNicks.getCache().getUUIDByName(event.getPlayer().getName());
                // nick belongs to a different UUID, force rename on nick-name
                if (!matchedUUID.equals(event.getPlayer().getUniqueId().toString())) {
                    OfflinePlayer nickNamedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(matchedUUID));
                    if (nickNamedPlayer.isOnline()) {
                        nickNamedPlayer.getPlayer().setDisplayName(null);
                        MessageUtil.sendMessage(nickNamedPlayer.getPlayer(), "messages.error.nickFixedByOwnerMessage");
                    }

                    SimplyNicks.getDatabase().removePlayerNameData(nickNamedPlayer.getPlayer());
                }
            }

            // check if logging in player has user nick
            String name = SimplyNicks.getDatabase().getPlayerNameData(event.getPlayer().getUniqueId().toString());
            if (name != null) {
                SimplyNicks.getCache().addNick(event.getPlayer().getUniqueId().toString(), name);
                event.getPlayer().setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
        }, 1L);
    }
}
