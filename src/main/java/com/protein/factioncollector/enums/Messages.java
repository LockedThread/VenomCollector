package com.protein.factioncollector.enums;

import org.bukkit.ChatColor;

public enum Messages {

    NO_PERMISSION("&c&l(!) &cNo permission!"),
    NOT_ONLINE("&c&l(!) &c{player} is not online!"),
    YOU_CANT_PLACE_HERE("&c&l(!) &cYou can't place this here!"),
    ALREADY_COLLECTOR_IN_CHUNK("&c&l(!) &cThere's already a Faction Collector in this chunk!"),
    GIVEN_COLLECTOR("&e&l(!) &eYou've given {player} {amount} faction collectors!"),
    GIVEN_REAPER_WAND("&e&l(!) &eYou've given {player} {amount} faction collectors!"),
    CANT_UNCLAIM("&c&l(!) &cYou're not able to unclaim land since you have a faction collector here!"),
    CANT_UNCLAIM_ALL("&c&l(!) &cYou're not able to unclaim all since you have a faction collectors placed in your land!"),
    CANT_DISBAND("&c&l(!) &cYou're not able to disband since you have a faction collectors placed in your land!"),
    SOLD("&2&l+{amount}"),
    STAFF_ALERT("&c&l(!) &c{player} could be abusing faction collectors. (VL:{vl})"),
    CLICKING_TOO_FAST("&c&l(!) &cYou are clicking too much calm down man!"),
    MUST_BE_PLACED_IN_CLAIMS("&c&l(!) &cFaction Collectors must be placed in your claim!");

    private String message;

    Messages(String message) {
        this.message = message;
    }

    public String getKey() {
        return name().toLowerCase().replace("_", "-");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
