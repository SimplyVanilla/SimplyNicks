package net.simplyvanilla.simplynicks.event;

import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.MessageUtil;
import net.simplyvanilla.simplynicks.util.NickUtil;
import org.bukkit.Bukkit;
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

                UUID matchedUUID = SimplyNicks.getCache().getUUIDByNick(event.getPlayer().getName());
                // nick belongs to a different UUID, force rename on nick-name
                if (matchedUUID != null && !matchedUUID.equals(event.getPlayer().getUniqueId())) {
                    OfflinePlayer nickNamedPlayer = Bukkit.getOfflinePlayer(matchedUUID);
                    if (nickNamedPlayer.isOnline()) {
                        nickNamedPlayer.getPlayer().displayName(null);
                        MessageUtil.sendMessage(nickNamedPlayer.getPlayer(), "messages.error.nickFixedByOwnerMessage");
                    }

                    SimplyNicks.getDatabase().removePlayerNickData(matchedUUID);

                    SimplyNicks.getInstance().getLogger().info(
                        String.format(
                            "%s name colliding with %s's nick, resetting...",
                            event.getPlayer().getName(),
                            matchedUUID
                        )
                    );
                }
            }

            // check if logging in player has user nick
            String nick = SimplyNicks.getDatabase().getPlayerNickData(event.getPlayer().getUniqueId());
            if (nick != null) {
                SimplyNicks.getCache().addNick(event.getPlayer().getUniqueId().toString(), nick);
                NickUtil.applyNick(event.getPlayer(), nick);
            }
        }, 1L);
    }
}
