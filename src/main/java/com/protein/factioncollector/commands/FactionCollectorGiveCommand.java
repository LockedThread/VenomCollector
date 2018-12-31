package com.protein.factioncollector.commands;

import com.protein.factioncollector.commands.arguments.ItemTypeArgument;
import com.protein.factioncollector.enums.ItemType;
import com.protein.factioncollector.enums.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.venompvp.venom.commands.Command;
import org.venompvp.venom.commands.arguments.Argument;
import org.venompvp.venom.commands.arguments.OptionalIntegerArgument;
import org.venompvp.venom.commands.arguments.PlayerArgument;
import org.venompvp.venom.module.Module;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class FactionCollectorGiveCommand extends Command {

    public FactionCollectorGiveCommand(Module module) {
        super(module,
                module.getCommandHandler().getCommand(FactionCollectorCommand.class),
                "give",
                "give players collector items",
                Arrays.asList(PlayerArgument.class, ItemTypeArgument.class, OptionalIntegerArgument.class),
                "factioncollector.give",
                false);
    }

    @Override
    public void execute(CommandSender sender, List<Argument> args, String label) {
        ItemType itemType = (ItemType) args.get(1).getValue();
        Player target = (Player) args.get(0).getValue();
        OptionalIntegerArgument optionalIntegerArgument = (OptionalIntegerArgument) args.get(2);
        int amount = optionalIntegerArgument.isPresent() ? optionalIntegerArgument.getValue() : 1;
        IntStream.range(0, amount).forEach(i -> target.getInventory().addItem(itemType.getItemStack()));
        sender.sendMessage(Messages.GIVEN.toString().replace("{player}", target.getName()).replace("{item-type}", amount == 1 ? itemType.toString() : itemType.toString() + "s").replace("{amount}", String.valueOf(amount)));
    }

    @Override
    public String getUsage(String label) {
        return "/collector " + label + " {player} {itemtype} ";
    }
}
