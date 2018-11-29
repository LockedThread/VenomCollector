package com.protein.factioncollector.listeners;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListener implements Listener {

    @EventHandler
    public void onLandUnclaim(LandUnclaimEvent event) {
        if (FactionCollector.getInstance().findCollector(event.getLocation().getWorld().getChunkAt((int) event.getLocation().getX(), (int) event.getLocation().getZ())) != null) {
            event.getfPlayer().sendMessage(Messages.CANT_UNCLAIM.toString());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLandUnclaimAll(LandUnclaimAllEvent event) {
        if (FactionCollector.getInstance().getCollectorLocations().stream().map(collector -> new FLocation(collector.getLocation())).anyMatch(fLocation -> event.getFaction().getAllClaims().contains(fLocation))) {
            event.getfPlayer().sendMessage(Messages.CANT_UNCLAIM_ALL.toString());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        if (FactionCollector.getInstance().getCollectorLocations().stream().map(collector -> new FLocation(collector.getLocation())).anyMatch(fLocation -> event.getFaction().getAllClaims().contains(fLocation))) {
            event.getFPlayer().sendMessage(Messages.CANT_DISBAND.toString());
            event.setCancelled(true);
        }
    }
}
