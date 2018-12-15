package com.protein.factioncollector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.ParentCommand;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.module.Module;

import java.util.Collections;
import java.util.List;

public class FactionCollectorCommand extends Command implements ParentCommand {

    public FactionCollectorCommand(Module module) {
        super(module,
                "factioncollector",
                "command to give people collector related items",
                Collections.emptyList(),
                "venom.factioncollector.give",
                false,
                "factioncollectors", "collector", "collectors");
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l[!] Faction Collector\n"
                + "&b/" + label + " list\n"
                + "&b/" + label + " give [player] [itemtype]"));
    }

    @Override
    public String getUsage(String label) {
        return "/" + label;
    }

    @Override
    public void setupSubCommands() {
        addSubCommands(new FactionCollectorListCommand(module), new FactionCollectorGiveCommand(module));
    }
}
