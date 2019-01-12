package com.protein.factioncollector.listeners;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.event.EventFactionsChunkChangeType;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListener implements Listener {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();

    @EventHandler
    public void onFactionDisband(EventFactionsDisband event) {
        BoardColl.get().getChunks(event.getFaction()).stream().filter(ps -> INSTANCE.findCollector(ps.asBukkitChunk()) != null).findAny().ifPresent(ps -> {
            event.getMPlayer().getPlayer().sendMessage(Messages.MUST_REMOVE_COLLECTORS.toString());
            event.setCancelled(true);
        });
    }

    @EventHandler
    public void onFactionsChunksChange(EventFactionsChunksChange event) {
        event.getChunkType().entrySet().stream().filter(entry -> entry.getValue() == EventFactionsChunkChangeType.SELL && INSTANCE.findCollector(entry.getKey().asBukkitChunk()) != null).findAny().ifPresent(entry -> {
            event.getMPlayer().getPlayer().sendMessage(Messages.MUST_REMOVE_COLLECTORS.toString());
            event.setCancelled(true);
        });
    }
}