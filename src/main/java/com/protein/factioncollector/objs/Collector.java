package com.protein.factioncollector.objs;

import com.google.gson.annotations.Expose;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.gui.CollectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Collector {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();

    @Expose()
    private ConcurrentHashMap<CollectionType, Integer> amounts;
    @Expose()
    private Location location;

    @Expose(serialize = false, deserialize = false)
    private ConcurrentLinkedQueue<UUID> viewers;
    @Expose(serialize = false, deserialize = false)
    private CollectorGUI collectorGUI;

    public Collector(Location location) {
        this.location = location;
        this.amounts = new ConcurrentHashMap<>();
        initIgnored();
    }

    public void initIgnored() {
        this.viewers = new ConcurrentLinkedQueue<>();
        this.collectorGUI = new CollectorGUI(this);
    }

    public Location getLocation() {
        return location;
    }

    public void subtractFromAmounts(CollectionType collectionType, int amount) {
        FactionCollector.getInstance().getVenom().executorService.submit(() -> {
            amounts.computeIfPresent(collectionType, (collectionType1, i) -> i -= amount);
            FactionCollector.getInstance().getServer().getScheduler().runTask(FactionCollector.getInstance(), () -> update(collectionType));
        });
    }

    public void addToAmounts(CollectionType collectionType, int amount) {
        FactionCollector.getInstance().getVenom().executorService.submit(() -> {
            amounts.computeIfPresent(collectionType, (collectionType1, i) -> i += amount);
            amounts.putIfAbsent(collectionType, amount);
            FactionCollector.getInstance().getServer().getScheduler().runTask(FactionCollector.getInstance(), () -> update(collectionType));
        });
    }

    public void setAmount(CollectionType collectionType, int amount) {
        amounts.put(collectionType, amount);
    }

    public void reset(CollectionType collectionType) {
        FactionCollector.getInstance().getVenom().executorService.submit(() -> {
            amounts.remove(collectionType);
        });
    }

    public int getAmount(CollectionType collectionType) {
        return amounts.getOrDefault(collectionType, 0);
    }

    public void update(CollectionType collectionType) {
        if (!viewers.isEmpty()) {
            viewers.stream().map(Bukkit::getPlayer).forEach(player -> {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                ItemStack[] itemStacks = inventory.getContents().clone();
                IntStream.range(0, itemStacks.length).filter(i -> itemStacks[i] != null).forEach(i -> {
                    ItemStack itemStack = itemStacks[i];
                    if ((itemStack.getType() == Material.MONSTER_EGG &&
                            collectionType.parseEntityType().isPresent() &&
                            EntityType.fromId(INSTANCE.getVenom().getSilkUtil().getStoredEggEntityID(itemStack)) == collectionType.parseEntityType().get()) ||
                            (collectionType.parseMaterial().isPresent() &&
                                    itemStack.getType() == collectionType.parseMaterial().get())) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setLore(INSTANCE.getConfig().getStringList("gui.item-template.lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("{amount}", String.valueOf(getAmount(collectionType))))).collect(Collectors.toList()));
                        itemStack.setItemMeta(itemMeta);
                        itemStacks[i] = itemStack;
                    }
                });
                inventory.setContents(itemStacks);
                player.updateInventory();
            });
        }
    }

    public CollectorGUI getCollectorGUI() {
        return collectorGUI;
    }

    public ConcurrentLinkedQueue<UUID> getViewers() {
        return viewers;
    }

    public ConcurrentHashMap<CollectionType, Integer> getAmounts() {
        return amounts;
    }
}
