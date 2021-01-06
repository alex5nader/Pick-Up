package dev.alexnader.pick_up.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;

public class HeldEntityItem extends Item {
    public HeldEntityItem(Settings settings) {
        super(settings);
    }

    public static ItemStack stackHolding(Entity entity) {
        ItemStack stack = new ItemStack(ITEMS.HELD_ENTITY_ITEM.value);
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag entityTag = new CompoundTag();
        entity.saveSelfToTag(entityTag);
        tag.put("entity", entityTag);
        return stack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usage) {
        World world = usage.getWorld();
        ItemStack stack = usage.getStack();
        ItemPlacementContext placement = new ItemPlacementContext(usage);

        if (placement.canPlace()) {
            CompoundTag entityTag = stack.getSubTag("entity");
            Vec3d pos = placement.getHitPos();
            System.out.println(pos);
            //noinspection ConstantConditions,OptionalGetWithoutIsPresent // .entity is required // invalid data should fail
            Entity entity = EntityType.getEntityFromTag(entityTag, world).get();
            entity.updatePosition(pos.getX(), pos.getY(), pos.getZ());
            world.spawnEntity(entity);

            //TODO: handle this?
            usage.getPlayer().getStackInHand(usage.getHand()).setCount(0);

            return ActionResult.success(world.isClient);
        }
        return super.useOnBlock(usage);
    }
}
