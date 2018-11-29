package com.protein.factioncollector.gui;

import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.enums.Messages;
import com.protein.factioncollector.objs.Collector;
import com.protein.factioncollector.utils.Utils;
import com.protein.lib.gui.Gui;
import com.protein.lib.gui.GuiButton;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;

public class FactionCollectorGUI extends Gui {

    private Collector collector;

    public FactionCollectorGUI(Collector collector) {
        super(FactionCollector.getInstance().getConfig().getString("gui.title"), FactionCollector.getInstance().getConfig().getInt("gui.size"));
        this.collector = collector;
    }

    @Override
    public void openInventory(Player player) {
        FactionCollector.getInstance().getGuiItemHashMap().forEach((key, value) -> {
            int threshold = FactionCollector.getInstance().random.nextInt(FactionCollector.getInstance().maxGuiClicksRange[0], FactionCollector.getInstance().maxGuiClicksRange[1]);
            ItemStack itemStack = value.clone();
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(meta.getDisplayName().replace("{mob}", StringUtils.capitalize(key.name().toLowerCase().replace("_", " "))));
            meta.setLore(meta.getLore().stream().map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("{amount}", String.valueOf(collector.getAmount(key))))).collect(Collectors.toList()));
            itemStack.setItemMeta(meta);
            addButtonAt(new GuiButton(itemStack).setGuiButtonListener(event -> {
                event.setCancelled(true);

                final boolean sameItem = Utils.isItemStack(event.getCursor(), FactionCollector.getInstance().getReaperWandItemStack());

                int amount = collector.getAmount(key);
                if (amount <= 0) return;

                int remainder = sub10OrReturn0(amount, sameItem ? 100 : 10), amountToBeSubtracted = sameItem ? 100 : 10;
                if (remainder > 0) amountToBeSubtracted = remainder;

                if (FactionCollector.getInstance().getGuiClicks().getOrDefault(player.getUniqueId(), 0) >= threshold) {
                    FactionCollector.getInstance().getGuiClickWarnings().computeIfPresent(player.getUniqueId(), (uuid, integer) -> {
                        if (FactionCollector.getInstance().guiClickThreshold <= integer) {
                            String vl = integer.toString();
                            Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("factioncollector.alerts"))
                                    .forEach(p -> p.sendMessage(Messages.STAFF_ALERT.toString().replace("{player}", p.getName()).replace("{vl}", vl)));
                        }
                        return integer + 1;
                    });
                    player.sendMessage(Messages.CLICKING_TOO_FAST.toString());
                    FactionCollector.getInstance().getGuiClickWarnings().putIfAbsent(player.getUniqueId(), 1);
                    player.closeInventory();
                    return;
                }

                collector.subtractFromAmounts(key, amountToBeSubtracted);

                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2f, 2f);

                FactionCollector.getInstance().getEcon().depositPlayer(player, FactionCollector.getInstance().getSellValues().get(key) * amountToBeSubtracted);

                player.sendMessage(Messages.SOLD.toString().replace("{amount}", String.valueOf(FactionCollector.getInstance().getSellValues().get(key) * amountToBeSubtracted)));
                FactionCollector.getInstance().getGuiClicks().computeIfPresent(player.getUniqueId(), (uuid, integer) -> integer += 1);
                FactionCollector.getInstance().getGuiClicks().putIfAbsent(player.getUniqueId(), 1);
            }), guiButtons.size());
        });
        collector.getViewers().add(player.getUniqueId());
        player.openInventory(getInventory());
        FactionCollector.getInstance().getGuiClicks().put(player.getUniqueId(), 0);
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        collector.getViewers().remove(event.getPlayer().getUniqueId());
    }

    private int sub10OrReturn0(int i, int divisor) {
        return i < 0 ? -1 : i % divisor > 0 && i < divisor ? i % divisor : 0;
    }
}
