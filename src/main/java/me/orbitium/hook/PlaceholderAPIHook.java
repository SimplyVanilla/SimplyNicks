package me.orbitium.hook;

import me.orbitium.SimplyNicks;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion
{
    public String getIdentifier() {
        return "simplynick";
    }
    
    public String getAuthor() {
        return "Orbit";
    }
    
    public String getVersion() {
        return "1.0.0";
    }
    
    public String onPlaceholderRequest(final Player player, final String identifier) {
        if (identifier.equalsIgnoreCase("name")) {
            return SimplyNicks.getCache().getName(player.getUniqueId().toString());
        }
        return null;
    }
}
