package com.protein.factioncollector.commands.arguments;

import com.protein.factioncollector.enums.ItemType;
import org.venompvp.venom.commands.arguments.Argument;

public class ItemTypeArgument extends Argument<ItemType> {

    public ItemTypeArgument(String check) {
        super(check);
    }

    @Override
    public ItemType getValue() {
        return super.getValue();
    }

    @Override
    public boolean isArgumentType() {
        if (check.equalsIgnoreCase("collector")) {
            setValue(ItemType.COLLECTOR);
            return true;
        }
        if (check.equalsIgnoreCase("sellwand")) {
            setValue(ItemType.SELL_WAND);
            return true;
        }
        if (check.equalsIgnoreCase("tntwand")) {
            setValue(ItemType.TNT_WAND);
            return true;
        }
        return false;
    }
}
