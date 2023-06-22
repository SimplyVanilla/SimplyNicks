package net.simplyvanilla.simplynicks.event;

import java.util.UUID;
import net.simplyvanilla.simplynicks.SimplyNicks;
import net.simplyvanilla.simplynicks.util.NickUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerEvents implements Listener {
    private final SimplyNicks plugin;

    public PlayerEvents(SimplyNicks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onPlayerLogin(PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {

            // check if logging in player-name is clashing with existing nick -> prefer player-name over nick
            if (!this.plugin.getCache().isNickAvailable(event.getPlayer().getName())) {

                UUID matchedUUID =
                    this.plugin.getCache().getUUIDByNick(event.getPlayer().getName());
                // nick belongs to a different UUID, force rename on nick-name
                if (matchedUUID != null && !matchedUUID.equals(event.getPlayer().getUniqueId())) {
                    OfflinePlayer nickNamedPlayer = Bukkit.getOfflinePlayer(matchedUUID);
                    if (nickNamedPlayer.isOnline()) {
                        nickNamedPlayer.getPlayer().displayName(null);
                        this.plugin.sendConfigMessage(nickNamedPlayer.getPlayer(),
                            "messages.error.nickFixedByOwnerMessage");
                    }

                    this.plugin.getDatabase().removePlayerNickData(matchedUUID);

                    this.plugin.getLogger().info(
                        String.format(
                            "%s name colliding with %s's nick, resetting...",
                            event.getPlayer().getName(),
                            matchedUUID
                        )
                    );
                }
            }

            // check if logging in player has user nick
            String nick =
                this.plugin.getDatabase().getPlayerNickData(event.getPlayer().getUniqueId());
            if (nick != null) {
                this.plugin.getCache().addNick(event.getPlayer().getUniqueId().toString(), nick);
                NickUtil.applyNick(event.getPlayer(), nick);
            }
        }, 1L);
    }
}
