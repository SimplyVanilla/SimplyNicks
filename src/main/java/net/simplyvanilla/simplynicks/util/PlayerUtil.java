package net.simplyvanilla.simplynicks.util;

import java.util.UUID;

public class PlayerUtil {
    public static UUID tryUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException exception){
            return null;
        }
    }
}
