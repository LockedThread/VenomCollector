package com.protein.lib.gui;

import org.bukkit.inventory.ItemStack;

public class GuiButton {

    private GuiButtonListener guiButtonListener;
    private ItemStack itemStack;

    public GuiButton(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public GuiButtonListener getGuiButtonListener() {
        return guiButtonListener;
    }

    public GuiButton setGuiButtonListener(GuiButtonListener guiButtonListener) {
        this.guiButtonListener = guiButtonListener;
        return this;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
