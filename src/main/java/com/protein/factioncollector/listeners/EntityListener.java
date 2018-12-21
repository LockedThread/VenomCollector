package com.protein.factioncollector.listeners;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.objs.Collector;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.venompvp.venom.Venom;
import org.venompvp.venom.utils.Utils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EntityListener implements Listener {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Collector collector = INSTANCE.findCollector(event.getClickedBlock().getChunk());
            if (collector != null && collector.getLocation().equals(event.getClickedBlock().getLocation())) {
                Player player = event.getPlayer();
                event.setCancelled(true);
                if (event.getItem() != null) {
                    if (Utils.isItem(event.getItem(), ItemType.SELL_WAND.getItemStack())) {
                        double money = collector.getAmounts()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey() != CollectionType.TNT && entry.getValue() > 0)
                                .mapToDouble(entry -> (entry.getValue() * entry.getKey().getValue()))
                                .sum();
                        if (money == 0) {
                            player.sendMessage(Messages.NOTHING_TO_SELL.toString());
                        } else {
                            collector.getAmounts().entrySet().stream().filter(entry -> entry.getKey() != CollectionType.TNT).forEach(entry -> collector.reset(entry.getKey()));
                            INSTANCE.getVenom().getEconomy().depositPlayer(player, money);
                            player.sendMessage(Messages.SOLD.toString().replace("{amount}", String.valueOf(money)));
                        }
                    } else if (Utils.isItem(event.getItem(), ItemType.TNT_WAND.getItemStack())) {
                        int amount = collector.getAmounts().entrySet().stream().filter(entry -> entry.getKey() == CollectionType.TNT).mapToInt(Map.Entry::getValue).sum();
                        if (amount == 0) {
                            player.sendMessage(Messages.NO_TNT_TO_DEPOSIT.toString());
                        } else {
                            collector.reset(CollectionType.TNT);
                            player.sendMessage(Messages.COLLECTED_TNT.toString().replace("{amount}", String.valueOf(amount)));

                            // TODO: Add faction bank shit 12/8/2018
                        }
                    } else {
                        collector.getCollectorGUI().openInventory(player);
                    }
                } else {
                    collector.getCollectorGUI().openInventory(player);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && event.getBlockPlaced() != null && event.getItemInHand() != null && Utils.isItem(event.getItemInHand(), ItemType.COLLECTOR.getItemStack())) {
            Player player = event.getPlayer();
            if (!canPlace(player, event.getBlockPlaced().getLocation())) {
                player.sendMessage(Messages.YOU_CANT_PLACE_HERE.toString());
                event.setCancelled(true);
            } else if (INSTANCE.findCollector(event.getBlockPlaced().getChunk()) != null) {
                player.sendMessage(Messages.ALREADY_COLLECTOR_IN_CHUNK.toString());
                event.setCancelled(true);
            } else {
                INSTANCE.getCollectors().add(new Collector(event.getBlockPlaced().getLocation()));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            Collector collector = INSTANCE.findCollector(event.getBlock().getChunk());
            if (collector != null && collector.getLocation().equals(event.getBlock().getLocation())) {
                if (!canPlace(event.getPlayer(), event.getBlock().getLocation())) {
                    event.getPlayer().sendMessage(Messages.YOU_CANT_PLACE_HERE.toString());
                    event.setCancelled(true);
                    return;
                }
                INSTANCE.getCollectors().remove(INSTANCE.findCollector(event.getBlock().getChunk()));
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity() != null) {
            final ItemStack itemStack = event.getEntity().getItemStack();
            if (itemStack != null && (itemStack.getType() == Material.SUGAR_CANE || itemStack.getType() == Material.CACTUS)) {
                event.setCancelled(true);
                Collector collector = INSTANCE.findCollector(event.getLocation().getChunk());
                if (collector != null) {
                    INSTANCE.findCollector(event.getLocation().getChunk()).addToAmounts(CollectionType.valueOf(itemStack.getType().name()), 1);
                }
            }
        }
    }

    @EventHandler
    public void onSpawnerPreSpawn(SpawnerPreSpawnEvent event) {
        if (INSTANCE.getWhiteListedCollectionTypes().contains(event.getSpawnedType().name()) || (INSTANCE.getWhiteListedCollectionTypes().contains("TNT")) && event.getSpawnedType() == EntityType.CREEPER) {
            Collector collector = INSTANCE.findCollector(event.getLocation().getChunk());
            if (collector != null) {
                event.setCancelled(true);
                if (event.getSpawnedType() == EntityType.CREEPER) {
                    final int i = INSTANCE.getVenom().random.nextInt(2);
                    if (i == 0) {
                        collector.addToAmounts(CollectionType.TNT, 1);
                    }
                } else {
                    CollectionType.fromEntityType(event.getSpawnedType()).ifPresent(collectionType1 -> collector.addToAmounts(collectionType1, 1));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        searchAndRemove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        searchAndRemove(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player &&
                event.getInventory() != null &&
                event.getInventory().getSize() == 36 &&
                event.getInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', INSTANCE.getConfig().getString("gui.title")))) {
            searchAndRemove((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player &&
                event.getCurrentItem() != null &&
                event.getClickedInventory() != null &&
                event.getClickedInventory().getSize() == 36 &&
                event.getClickedInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', INSTANCE.getConfig().getString("gui.title")))) {
            event.setCancelled(true);
            final Player player = (Player) event.getWhoClicked();
            final Optional<CollectionType> collectionType = getCollectionType(event.getCurrentItem());
            collectionType.ifPresent(collectionType1 -> {
                for (Collector collector : INSTANCE.getCollectors()) {
                    for (UUID viewer : collector.getViewers()) {
                        if (player.getUniqueId().toString().equals(viewer.toString())) {
                            int amount = collector.getAmount(collectionType1);
                            if (amount > 0) {

                                int remainder = sub10OrReturn0(amount, collectionType.get() == CollectionType.TNT ? 64 : 10), amountToBeSubtracted = collectionType.get() == CollectionType.TNT ? 64 : 10;
                                if (remainder > 0) amountToBeSubtracted = remainder;

                                if (collectionType.get() == CollectionType.TNT) {
                                    if (player.getInventory().firstEmpty() != -1) {
                                        player.getInventory().addItem(new ItemStack(Material.TNT, amountToBeSubtracted));
                                    } else {
                                        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.TNT, amountToBeSubtracted));
                                    }
                                } else {
                                    INSTANCE.getVenom().getEconomy().depositPlayer(player, collectionType1.getValue() * amountToBeSubtracted);
                                    player.sendMessage(Messages.SOLD.toString().replace("{amount}", String.valueOf(collectionType1.getValue() * amountToBeSubtracted)));
                                }
                                collector.subtractFromAmounts(collectionType1, amountToBeSubtracted);
                                collector.update(collectionType1);
                            }
                        }
                    }
                }
            });
        }
    }

    private Optional<CollectionType> getCollectionType(ItemStack itemStack) {
        CollectionType collectionType;
        switch (itemStack.getType()) {
            case CACTUS:
                collectionType = CollectionType.CACTUS;
                break;
            case SUGAR_CANE:
                collectionType = CollectionType.SUGAR_CANE;
                break;
            case TNT:
                collectionType = CollectionType.TNT;
                break;
            case MONSTER_EGG:
                collectionType = CollectionType.valueOf(EntityType.fromId(INSTANCE.getVenom().getSilkUtil().getStoredEggEntityID(itemStack)).name());
                break;
            default:
                return Optional.empty();
        }
        return Optional.of(collectionType);
    }

    private void searchAndRemove(Player player) {
        INSTANCE.getServer().getScheduler().runTaskAsynchronously(INSTANCE, () -> INSTANCE.getCollectors().forEach(collector -> collector.getViewers().stream().filter(viewer -> viewer.toString().equals(player.getUniqueId().toString())).forEach(viewer -> collector.getViewers().remove(viewer))));
    }

    private int sub10OrReturn0(int i, int divisor) {
        return i < 0 ? -1 : i % divisor > 0 && i < divisor ? i % divisor : 0;
    }

    private boolean canPlace(Player player, Location location) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction target = Utils.getFactionAt(location);
        return !target.isNone() && Venom.getInstance().getWorldGuardPlugin().canBuild(player, location) && target.getName().equals(mPlayer.getFaction().getName()) &&
                mPlayer.getFaction().isPermitted(MPerm.getPermBuild(), mPlayer.getRelationTo(target));
    }
}
