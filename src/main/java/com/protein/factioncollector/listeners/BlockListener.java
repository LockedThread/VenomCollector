package com.protein.factioncollector.listeners;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.objs.Collector;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EnumDirection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.venompvp.venom.Venom;
import org.venompvp.venom.utils.Utils;

import java.util.Arrays;

public class BlockListener implements Listener {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();


    public boolean canGrow(World bukkitWorld, Block bukkitBlock) {
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) bukkitWorld).getHandle();

        for (EnumDirection enumDirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            if (nmsWorld.getType(blockPosition.shift(enumDirection)).getBlock().getMaterial().isBuildable()) {
                return false;
            }
        }

        final net.minecraft.server.v1_8_R3.Block block = nmsWorld.getType(blockPosition.down()).getBlock();
        return block == Blocks.CACTUS || block == Blocks.SAND;
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (!canGrow(event.getBlock().getWorld(), event.getBlock())) {
            if (event.getNewState().getType() == Material.CACTUS) {
                event.setCancelled(true);
                final Collector collector = INSTANCE.findCollector(event.getBlock().getChunk());
                if (collector != null) {
                    collector.addToAmounts(CollectionType.CACTUS, 1);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && event.getBlockPlaced() != null && event.getItemInHand() != null) {
            Location location = event.getBlockPlaced().getLocation();
            Player player = event.getPlayer();
            if (Utils.isItem(event.getItemInHand(), ItemType.COLLECTOR.getItemStack())) {
                if (Utils.getFactionAt(location).isNone()) {
                    player.sendMessage(Messages.CANT_PLACE_IN_WILDERNESS.toString());
                    event.setCancelled(true);
                } else if (!canPlace(player, location)) {
                    player.sendMessage(Messages.YOU_CANT_PLACE_HERE.toString());
                    event.setCancelled(true);
                } else if (INSTANCE.containsCollector(location.getChunk())) {
                    player.sendMessage(Messages.ALREADY_COLLECTOR_IN_CHUNK.toString());
                    event.setCancelled(true);
                } else {
                    INSTANCE.getCollectorHashMap().put(INSTANCE.chunkToString(location.getChunk()), new Collector(location));
                }
            } else if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER) {
                final BlockState[] tileEntities = location.getChunk().getTileEntities();
                final ItemStack hand = event.getItemInHand().clone();
                Bukkit.getScheduler().runTaskAsynchronously(INSTANCE, () -> {
                    int spawnerAmount = (int) Arrays.stream(tileEntities).filter(tile -> tile.getType() == Material.MOB_SPAWNER).count();
                    if (spawnerAmount > 250) {
                        Utils.editBlockType(location, Material.AIR);
                        player.sendMessage(Messages.EXCEEDED_MAX_SPAWNERS.toString());
                        hand.setAmount(1);
                        player.getInventory().addItem(hand);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().stream().filter(block -> block.getType() == Material.BEACON).forEach(block -> event.blockList().remove(block));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!event.isCancelled()) {
            if (block.getType() == Material.SUGAR_CANE_BLOCK) {
                final Collector collector = INSTANCE.findCollector(block.getChunk());
                if (collector == null) {
                    player.sendMessage(Messages.ONLY_USED_IN_COLLECTOR_CHUNK.toString());
                    event.setCancelled(true);
                    return;
                }
                final boolean isItem = Utils.isItem(event.getPlayer().getItemInHand(), ItemType.HARVESTER_HOE.getItemStack());
                int a = 0;
                Block next = block;
                while (next != null && next.getType() == Material.SUGAR_CANE_BLOCK) {
                    Utils.editBlockType(next.getLocation(), Material.AIR);
                    a += isItem ? 2 : 1;
                    next = next.getRelative(BlockFace.UP);
                }
                collector.addToAmounts(CollectionType.SUGAR_CANE, a);
                event.setCancelled(true);
                return;
            }
            final Collector collector = INSTANCE.findCollector(block.getChunk());
            if (collector != null && collector.getLocation().equals(block.getLocation())) {
                double sum = collector.getAmounts()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey() != CollectionType.TNT && entry.getValue() > 0)
                        .mapToDouble(entry -> (entry.getValue() * entry.getKey().getValue()))
                        .sum();

                INSTANCE.getVenom().getEconomy().depositPlayer(player, sum);
                player.sendMessage(Messages.SOLD.toString().replace("{amount}", String.valueOf(sum)));
                INSTANCE.getCollectorHashMap().remove(INSTANCE.chunkToString(block.getChunk()));
                block.getWorld().dropItemNaturally(block.getLocation(), ItemType.COLLECTOR.getItemStack());
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    private boolean canPlace(Player player, Location location) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction target = Utils.getFactionAt(location);
        return !target.isNone() && Venom.getInstance().getWorldGuardPlugin().canBuild(player, location) && target.getName().equals(mPlayer.getFaction().getName()) &&
                mPlayer.getFaction().isPermitted(MPerm.getPermBuild(), mPlayer.getRelationTo(target));
    }
}
