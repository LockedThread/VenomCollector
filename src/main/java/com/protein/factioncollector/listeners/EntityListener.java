package com.protein.factioncollector.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.gui.FactionCollectorGUI;
import com.protein.factioncollector.objs.Collector;
import com.protein.factioncollector.utils.Utils;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Collector collector = FactionCollector.getInstance().findCollector(event.getClickedBlock().getChunk());
            if (collector != null && collector.getLocation().equals(event.getClickedBlock().getLocation())) {
                new FactionCollectorGUI(collector).openInventory(event.getPlayer());
                event.setCancelled(true);
            }
            if (event.getItem() != null && event.getItem().getType() == Material.MONSTER_EGG) {
                event.getPlayer().sendMessage(String.valueOf(event.getItem().getTypeId()));
                event.getPlayer().sendMessage(String.valueOf(event.getItem().getDurability()));
                event.getPlayer().sendMessage(String.valueOf(event.getItem().getData().toString()));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && event.getBlockPlaced() != null && event.getItemInHand() != null && Utils.isItemStack(event.getItemInHand(), FactionCollector.getInstance().getCollectorItemStack())) {
            Player player = event.getPlayer();
            if (!Utils.canBuild(player, event.getBlockPlaced().getLocation())) {
                player.sendMessage(Messages.YOU_CANT_PLACE_HERE.toString());
                event.setCancelled(true);
                return;
            }
            if (FPlayers.getInstance().getByPlayer(player).getRelationTo(Board.getInstance().getFactionAt(new FLocation(event.getBlockPlaced().getLocation()))) != Relation.MEMBER) {
                player.sendMessage(Messages.MUST_BE_PLACED_IN_CLAIMS.toString());
                event.setCancelled(true);
                return;
            }
            if (FactionCollector.getInstance().findCollector(event.getBlockPlaced().getChunk()) != null) {
                player.sendMessage(Messages.ALREADY_COLLECTOR_IN_CHUNK.toString());
                event.setCancelled(true);
                return;
            }
            FactionCollector.getInstance().getCollectorLocations().add(new Collector(event.getBlockPlaced().getLocation()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            if (!Utils.canBuild(event.getPlayer(), event.getBlock().getLocation())) {
                event.getPlayer().sendMessage(Messages.YOU_CANT_PLACE_HERE.toString());
                event.setCancelled(true);
                return;
            }
            Collector collector = FactionCollector.getInstance().findCollector(event.getBlock().getChunk());
            if (collector != null && collector.getLocation().equals(event.getBlock().getLocation())) {
                FactionCollector.getInstance().getCollectorLocations().remove(FactionCollector.getInstance().findCollector(event.getBlock().getChunk()));
            }
        }
    }

    @EventHandler
    public void onSpawnerPreSpawn(SpawnerPreSpawnEvent event) {
        if (FactionCollector.getInstance().getWhiteListedMobs().contains(event.getSpawnedType().name())) {
            Collector collector = FactionCollector.getInstance().findCollector(event.getLocation().getChunk());
            if (collector != null) {
                event.setCancelled(true);
                collector.addToAmounts(event.getSpawnedType(), 1);
            }
        }
    }
}
