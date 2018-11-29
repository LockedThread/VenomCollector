package com.protein.lib.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player &&
                event.getClickedInventory() != null &&
                event.getClickedInventory().getHolder() != null &&
                event.getClickedInventory().getHolder() instanceof Gui) {
            GuiButton button = ((Gui) event.getInventory().getHolder()).getButtonAt(event.getSlot());
            if (button != null && button.getGuiButtonListener() != null) {
                button.getGuiButtonListener().onClick(event);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != null &&
                event.getInventory().getHolder() != null &&
                event.getInventory().getHolder() instanceof Gui) {
            Gui gui = (Gui) event.getInventory().getHolder();
            if (gui != null) {
                gui.onInventoryClose(event);
            }
        }
    }
}

