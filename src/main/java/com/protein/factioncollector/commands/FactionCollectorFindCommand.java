package com.protein.factioncollector.commands;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.protein.factioncollector.FactionCollector;
import com.protein.factioncollector.queue.FindCollectorTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.commands.arguments.FactionArgument;
import org.venompvp.venom.module.Module;
import org.venompvp.venom.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FactionCollectorFindCommand extends Command {

    public FactionCollectorFindCommand(Module module) {
        super(module,
                module.getCommandHandler().getCommand(FactionCollectorCommand.class),
                "find",
                "Finds locations of faction collections in faction territory",
                Collections.singletonList(FactionArgument.class),
                "factioncollector.find",
                "lookup");
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        final Faction faction = (Faction) args.get(0).getValue();

        sender.sendMessage(ChatColor.YELLOW + "Searching for collectors in " + faction.getName() + ".....");
        new FindCollectorTask(BoardColl.get().getChunks(faction), 20, t -> Bukkit.getScheduler().runTaskAsynchronously(module, () -> {
            final String info = t
                    .stream()
                    .map(chunk -> FactionCollector.getInstance().getCollectorHashMap().get(FactionCollector.getInstance().chunkToString(chunk)))
                    .filter(Objects::nonNull)
                    .map(collector -> "World: " + collector.getLocation().getWorld().getName() +
                            " X:" + collector.getLocation().getBlockX() +
                            " Y:" + collector.getLocation().getBlockY() +
                            " Z:" + collector.getLocation().getBlockZ() + "\n")
                    .collect(Collectors.joining());

            sender.sendMessage(info.isEmpty() ? ChatColor.RED + "This faction does not have any collectors!" : ChatColor.YELLOW + Utils.saveTextToHastebin(info));
        })).runTaskTimer(module, 0L, 10L);

    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label + " [faction]";
    }
}
