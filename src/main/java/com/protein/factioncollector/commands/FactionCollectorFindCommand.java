package com.protein.factioncollector.commands;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.protein.factioncollector.FactionCollector;
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
        Bukkit.getScheduler().runTaskAsynchronously(module, () -> {
            Faction faction = (Faction) args.get(0).getValue();
            String stringBuilder = BoardColl.get()
                    .getChunks(faction)
                    .stream()
                    .map(ps -> FactionCollector.getInstance().getCollectorHashMap().get(FactionCollector.getInstance().chunkToString(ps.asBukkitChunk())))
                    .filter(Objects::nonNull)
                    .map(collector -> "World: " + collector.getLocation().getWorld().getName() +
                            " X:" + collector.getLocation().getBlockX() +
                            " Y:" + collector.getLocation().getBlockY() +
                            " Z:" + collector.getLocation().getBlockZ() + "\n")
                    .collect(Collectors.joining());
            sender.sendMessage(stringBuilder.isEmpty() ? ChatColor.RED + "This faction does not have any collectors!" : ChatColor.YELLOW + Utils.saveTextToHastebin(stringBuilder));
        });
    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label + " [faction]";
    }
}
