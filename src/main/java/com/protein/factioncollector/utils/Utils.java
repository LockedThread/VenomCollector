package com.protein.factioncollector.utils;

import com.google.common.base.Joiner;
import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.protein.factioncollector.FactionCollector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static ItemStack configSectionToItemStack(String where) {
        FileConfiguration c = FactionCollector.getInstance().getConfig();
        ItemStack itemStack = new ItemStack(Material.matchMaterial(c.getString(where + ".material")));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', c.getString(where + ".name")));
        meta.setLore(c.getStringList(where + ".lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static boolean isItemStack(ItemStack a, ItemStack b) {
        if (a != null && b != null && a.getType() == b.getType()) {
            if (a.hasItemMeta() && b.hasItemMeta()) {
                if (a.getItemMeta().getDisplayName().equalsIgnoreCase(b.getItemMeta().getDisplayName())) {
                    return a.getItemMeta().getLore().equals(b.getItemMeta().getLore());
                }
            } else return !a.hasItemMeta() && !b.hasItemMeta();
        }
        return false;
    }

    public static boolean canBuild(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(location));
        if (otherFaction.isSafeZone() || otherFaction.isWarZone()) return false;
        if (FactionCollector.getInstance().getWorldGuardPlugin().canBuild(player, location)) {
            if (otherFaction.isWilderness()) {
                return true;
            }
            if (fPlayer.getRelationTo(otherFaction) == Relation.MEMBER) {
                Access access = fPlayer.getFaction().getAccess(fPlayer, PermissableAction.BUILD);
                if (access != null && access != Access.DENY) {
                    return access == Access.ALLOW || access == Access.UNDEFINED;
                }
            }
        }
        return false;
    }

    public static String toPrettyList(List<String> list) {
        return list.size() > 1 ? Joiner.on(", ").join(list.subList(0, list.size() - 1))
                .concat(String.format("%s and ", list.size() > 2 ? "," : ""))
                .concat(list.get(list.size() - 1)) : list.get(0);
    }
}
