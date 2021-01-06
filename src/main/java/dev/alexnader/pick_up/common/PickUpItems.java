package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import dev.alexnader.pick_up.common.item.HeldEntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.pick_up.common.PickUp.META;

public class PickUpItems extends Registrar<Item> {
    public PickUpItems() {
        super(Registry.ITEM);
    }

    public final Id<HeldBlockItem> HELD_BLOCK_ITEM = register(new HeldBlockItem(new Item.Settings().maxCount(1)), META.id("held_block"));
    public final Id<HeldEntityItem> HELD_ENTITY_ITEM = register(new HeldEntityItem(new Item.Settings().maxCount(1)), META.id("held_entity"));
}
