package com.protein.factioncollector.commands;

import com.protein.factioncollector.commands.arguments.ItemTypeArgument;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.commands.arguments.PlayerArgument;
import org.venompvp.venom.module.Module;

import java.util.Arrays;
import java.util.List;

public class FactionCollectorGiveCommand extends Command {

    public FactionCollectorGiveCommand(Module module) {
        super(module,
                module.getCommandHandler().getCommand(FactionCollectorCommand.class),
                "give",
                "give players collector items",
                Arrays.asList(PlayerArgument.class, ItemTypeArgument.class),
                "venom.factioncollector.give",
                false);
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        for (Argument arg : args) {
            System.out.println(arg.getValue().toString());
        }
        ItemType itemType = (ItemType) args.get(1).getValue();
        Player target = (Player) args.get(0).getValue();
        target.getInventory().addItem(itemType.getItemStack());
        sender.sendMessage(Messages.GIVEN.toString().replace("{player}", target.getName()).replace("{item-type}", itemType.toString()));
    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label + " {player} {itemtype} ";
    }
}
