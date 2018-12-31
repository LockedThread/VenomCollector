package com.protein.factioncollector.commands;

import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.objs.Collector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.module.Module;

import java.util.Collections;
import java.util.List;

public class FactionCollectorRemoveCommand extends Command {

    public FactionCollectorRemoveCommand(Module module) {
        super(module,
                module.getCommandHandler().getCommand(FactionCollectorRemoveCommand.class),
                "remove",
                "Removes the collector in the player's chunk",
                Collections.emptyList(),
                "factioncollector.remove", true);
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        Player player = (Player) sender;
        String chunkString = FactionCollector.getInstance().chunkToString(player.getLocation().getChunk());
        Collector collector = FactionCollector.getInstance().getCollectorHashMap().get(chunkString);
        if (collector != null) {
            player.sendMessage(ChatColor.RED + "Removed Collector @ " +
                    collector.getLocation().getWorld().getName() + ":" +
                    collector.getLocation().getBlockX() + ":" +
                    collector.getLocation().getBlockY() + ":" +
                    collector.getLocation().getBlockZ());
            collector.getLocation().getBlock().setType(Material.AIR);
            FactionCollector.getInstance().getCollectorHashMap().remove(chunkString);
        } else {
            sender.sendMessage(ChatColor.RED + "There's no collector in this chunk!");
        }
    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label;
    }
}
