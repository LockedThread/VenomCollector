package com.protein.factioncollector.objs;

import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.utils.GsonFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Collector {

    private HashMap<EntityType, Integer> amounts;
    private Location location;

    @GsonFactory.Ignore
    private ArrayList<UUID> viewers;

    public Collector(Location location) {
        this.location = location;
        this.amounts = new HashMap<>();
        initIgnored();
    }

    public void initIgnored() {
        this.viewers = new ArrayList<>();
    }

    public Location getLocation() {
        return location;
    }

    public void subtractFromAmounts(EntityType entityType, int amount) {
        amounts.computeIfPresent(entityType, (entityType1, i) -> i -= amount);
        update(entityType);
    }

    public void addToAmounts(EntityType entityType, int amount) {
        amounts.computeIfPresent(entityType, (entityType1, i) -> i += amount);
        amounts.putIfAbsent(entityType, amount);
        update(entityType);
    }

    public void setAmount(EntityType entityType, int amount) {
        amounts.put(entityType, amount);
    }

    public void reset(EntityType entityType) {
        amounts.remove(entityType);
    }

    public int getAmount(EntityType entityType) {
        return amounts.getOrDefault(entityType, 0);
    }

    private void update(EntityType entityType) {
        if (!viewers.isEmpty()) {
            viewers.stream().map(Bukkit::getPlayer).forEach(player -> {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                ItemStack[] itemStacks = inventory.getContents().clone();
                IntStream.range(0, itemStacks.length).filter(i -> itemStacks[i] != null).forEach(i -> {
                    ItemStack itemStack = itemStacks[i];
                    if (itemStack.getType() == Material.MONSTER_EGG && EntityType.fromId(FactionCollector.getInstance().getSilkUtil().getStoredEggEntityID(itemStack)) == entityType) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setLore(FactionCollector.getInstance().getConfig().getStringList("gui.item-template.lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("{amount}", String.valueOf(getAmount(entityType))))).collect(Collectors.toList()));
                        itemStack.setItemMeta(itemMeta);
                    }
                    itemStacks[i] = itemStack;
                });
                inventory.setContents(itemStacks);
                player.updateInventory();
            });
        }
    }

    public ArrayList<UUID> getViewers() {
        return viewers;
    }
}
