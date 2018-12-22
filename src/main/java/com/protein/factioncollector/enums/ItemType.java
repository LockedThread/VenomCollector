package com.protein.factioncollector.enums;

import org.bukkit.inventory.ItemStack;
import org.venompvp.venom.utils.Utils;

public enum ItemType {

    HARVESTER_HOE,
    SELL_WAND,
    TNT_WAND,
    COLLECTOR,
    GUI_BACKGROUND,
    INFO;

    private ItemStack itemStack;

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public String toString() {
        return Utils.capitalizeEveryWord(name().replace("_", " "));
    }
}
