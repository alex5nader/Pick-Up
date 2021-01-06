package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.pick_up.common.PickUp.META;

public class PickUpItems extends Registrar<Item> {
    public PickUpItems() {
        super(Registry.ITEM);
    }

    public final Id<HeldBlockItem> HELD_BLOCK_ITEM = register(new HeldBlockItem(new Item.Settings().maxCount(1)), META.id("held_block"));
}
