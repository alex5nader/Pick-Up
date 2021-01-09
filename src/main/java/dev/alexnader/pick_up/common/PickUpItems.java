package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import dev.alexnader.pick_up.common.item.HeldEntityItem;
import dev.alexnader.pick_up.mixinterface.DenylistSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static dev.alexnader.pick_up.common.PickUp.META;

public class PickUpItems extends Registrar<Item> {
    public final Id<HeldBlockItem> HELD_BLOCK_ITEM = register(new HeldBlockItem(new Item.Settings().maxCount(1)), META.id("held_block"));
    public final Id<HeldEntityItem> HELD_ENTITY_ITEM = register(new HeldEntityItem(new Item.Settings().maxCount(1)), META.id("held_entity"));

    public PickUpItems() {
        super(Registry.ITEM);
        register(new Item(new Item.Settings()) {
            @Override
            public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
                System.out.println(((DenylistSource) world).denylist());
                return super.use(world, user, hand);
            }
        }, META.id("test"));
    }

    public boolean isHeldItem(ItemConvertible item) {
        Item asItem = item.asItem();
        return asItem == HELD_BLOCK_ITEM.value || asItem == HELD_ENTITY_ITEM.value;
    }
}
