package com.protein.factioncollector.gui;

import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.CollectionType;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.objs.Collector;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.venompvp.venom.utils.Utils;

import java.util.stream.Collectors;

public class CollectorGUI {

    private static final FactionCollector INSTANCE = FactionCollector.getInstance();

    private Collector collector;

    public CollectorGUI(Collector collector) {
        this.collector = collector;
    }

    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', INSTANCE.getConfig().getString("gui.title")));
        inventory.setItem(4, ItemType.INFO.getItemStack());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i <= 8 || ((i + 1) % 9 == 0 || (i + 1) % 9 == 1) || (i >= 26 && i <= 36) || (i <= 11 && i >= 10) || (i >= 15 && i <= 18)) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    inventory.setItem(i, ItemType.GUI_BACKGROUND.getItemStack());
                }
            }
        }
        INSTANCE.getGuiItemHashMap().entrySet().stream().map(collectionTypeItemStackEntry -> collectionTypeItemStackEntry.getValue().clone()).forEach(itemStack -> {
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
                default:
                    collectionType = CollectionType.valueOf(EntityType.fromId(FactionCollector.getInstance().getVenom().getSilkUtil().getStoredEggEntityID(itemStack)).name());
                    break;
            }
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(meta.getDisplayName().replace("{mob}", StringUtils.capitalize(Utils.capitalizeEveryWord(collectionType.name().replace("_", " ")))));
            meta.setLore(meta.getLore().stream().map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("{amount}", String.valueOf(collector.getAmount(collectionType))))).collect(Collectors.toList()));
            itemStack.setItemMeta(meta);

            inventory.addItem(itemStack);
        });
        collector.getViewers().add(player.getUniqueId());
        player.openInventory(inventory);
    }
}
