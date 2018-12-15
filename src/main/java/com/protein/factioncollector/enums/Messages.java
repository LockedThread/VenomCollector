package com.protein.factioncollector.enums;

import org.bukkit.ChatColor;

public enum Messages {

    NO_PERMISSION("&c&l(!) &cNo permission!"),
    NOT_ONLINE("&c&l(!) &c{player} is not online!"),
    YOU_CANT_PLACE_HERE("&c&l(!) &cYou can't place this here!"),
    ALREADY_COLLECTOR_IN_CHUNK("&c&l(!) &cThere's already a Faction Collector in this chunk!"),
    SOLD("&2&l+{amount}"),
    ERROR("&c&l(!) &cError parsing command!"),
    GIVEN("&a&l(!) &aYou've given {player} a {item-type}!"),
    COLLECTED_TNT("&a&l(!) &aYou've deposited {amount} to your faction's TNTBank!"),
    NO_TNT_TO_DEPOSIT("&c&l(!) &cYou don't have any TNT to deposit");

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
