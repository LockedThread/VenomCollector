package com.protein.lib.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

public abstract class Gui implements InventoryHolder {

    public String title;
    public HashMap<Integer, GuiButton> guiButtons;
    public int size;

    public Gui(String title, int size) {
        this.title = title;
        this.size = size;
        this.guiButtons = new HashMap<>();
    }

    public void addButtonAt(GuiButton guiButton, int slot) {
        guiButtons.put(slot, guiButton);
    }

    public GuiButton getButtonAt(int slot) {
        return guiButtons.get(slot);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, size, ChatColor.translateAlternateColorCodes('&', title));
        guiButtons.keySet().forEach(slot -> inventory.setItem(slot, guiButtons.get(slot).getItemStack()));
        return inventory;
    }

    public abstract void onInventoryClose(InventoryCloseEvent event);

    public abstract void openInventory(Player player);
}
