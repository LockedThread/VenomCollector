package com.protein.factioncollector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.module.Module;

import java.util.Collections;
import java.util.List;

public class FactionCollectorListCommand extends Command {

    public FactionCollectorListCommand(Module module) {
        super(module,
                module.getCommandHandler().getCommand(FactionCollectorCommand.class),
                "list",
                "Lists itemtypes",
                Collections.emptyList(),
                "venom.factioncollector.give",
                false);
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l(!) &eItemTypes: &ccollector, sellwand, tntwand, harvesterhoe"));
    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label;
    }
}
