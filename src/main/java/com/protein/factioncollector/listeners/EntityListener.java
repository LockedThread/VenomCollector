package com.protein.factioncollector.listeners;

import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.objs.Collector;
import me.aceix8.outposts.AceOutposts;
import me.aceix8.outposts.api.OutpostAPI;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.venompvp.venom.utils.Utils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EntityListener implements Listener {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();
    private static final OutpostAPI OUTPOST_API = AceOutposts.getInstance().getApi();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.BEACON) {
                final Collector collector = INSTANCE.findCollector(block.getChunk());
                if (collector != null && Utils.compareLocations(collector.getLocation(), block.getLocation())) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
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
                                    if (OUTPOST_API.isFactionControllingAnOutpost(Utils.getFactionByPlayer(player))) {
                                        money *= 2;
                                    }
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
                                    Utils.getFactionByPlayer(player).addAmountToTntBank(amount);
                                }
                            } else {
                                collector.getCollectorGUI().openInventory(player);
                            }
                        } else
                            collector.getCollectorGUI().openInventory(player);
                    } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !Utils.canEdit(player, block.getLocation())) {
                        event.setCancelled(true);
                        INSTANCE.getCollectorHashMap().remove(INSTANCE.chunkToString(block.getChunk()));
                        block.setType(Material.AIR);
                        block.getWorld().dropItemNaturally(block.getLocation(), ItemType.COLLECTOR.getItemStack());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity() != null) {
            final ItemStack itemStack = event.getEntity().getItemStack();
            if (itemStack != null && (itemStack.getType() == Material.SUGAR_CANE || itemStack.getType() == Material.CACTUS)) {
                event.setCancelled(true);
                INSTANCE.getServer().getScheduler().runTaskAsynchronously(INSTANCE, () -> {
                    final Collector collector = INSTANCE.findCollector(event.getLocation().getChunk());
                    if (collector != null) {
                        collector.addToAmounts(CollectionType.valueOf(itemStack.getType().name()), 1);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onSpawnerPreSpawn(SpawnerPreSpawnEvent event) {
        if (INSTANCE.getWhiteListedCollectionTypes().contains(event.getSpawnedType().name()) ||
                (INSTANCE.getWhiteListedCollectionTypes().contains("TNT")) && event.getSpawnedType() == EntityType.CREEPER) {
            event.setCancelled(true);
            INSTANCE.getServer().getScheduler().runTaskAsynchronously(INSTANCE, () -> {
                Collector collector = INSTANCE.findCollector(event.getLocation().getChunk());
                if (collector != null) {
                    if (event.getSpawnedType() == EntityType.CREEPER) {
                        final int i = INSTANCE.getVenom().random.nextInt(2);
                        if (i == 0) {
                            collector.addToAmounts(CollectionType.TNT, 1);
                        }
                    } else {
                        collector.addToAmounts(CollectionType.fromEntityType(event.getSpawnedType()), 1);
                    }
                }
            });
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
            collectionType.ifPresent(collectionType1 -> INSTANCE.getCollectorHashMap().forEach((key, value) -> {
                for (UUID viewer : value.getViewers()) {
                    if (player.getUniqueId().toString().equals(viewer.toString())) {
                        int amount = value.getAmount(collectionType1);
                        if (amount > 0) {
                            int remainder = Utils.sub10OrReturn0(amount, collectionType.get() == CollectionType.TNT ? 64 : INSTANCE.getConfig().getInt("sell-quantity")),
                                    amountToBeSubtracted = collectionType.get() == CollectionType.TNT ? 64 : INSTANCE.getConfig().getInt("sell-quantity");
                            if (remainder > 0) amountToBeSubtracted = remainder;
                            if (collectionType.get() == CollectionType.TNT) {
                                if (player.getInventory().firstEmpty() != -1) {
                                    player.getInventory().addItem(new ItemStack(Material.TNT, amountToBeSubtracted));
                                } else {
                                    player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.TNT, amountToBeSubtracted));
                                }
                            } else {
                                double shmoney = (collectionType1.getValue() * amountToBeSubtracted) * (OUTPOST_API.isFactionControllingAnOutpost(Utils.getFactionByPlayer(player)) ? 2 : 1);
                                INSTANCE.getVenom().getEconomy().depositPlayer(player, shmoney);
                                player.sendMessage(Messages.SOLD.toString().replace("{amount}", String.valueOf(shmoney)));
                            }
                            value.subtractFromAmounts(collectionType1, amountToBeSubtracted);
                            value.update(collectionType1);
                        }
                    }
                }
            }));
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
        INSTANCE
                .getServer()
                .getScheduler()
                .runTaskAsynchronously(INSTANCE, () -> INSTANCE.getCollectorHashMap()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getViewers().contains(player.getUniqueId()))
                        .findFirst()
                        .ifPresent(entry -> entry.getValue().getViewers().remove(player.getUniqueId())));
    }
}
