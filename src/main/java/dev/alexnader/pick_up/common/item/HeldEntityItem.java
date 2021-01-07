package dev.alexnader.pick_up.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;
import static dev.alexnader.pick_up.common.PickUp.META;

public class HeldEntityItem extends Item implements HeldItem {
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

    public static Entity getEntity(ItemStack stack, World world) {
        CompoundTag entityTag = stack.getSubTag("entity");
        //noinspection ConstantConditions,OptionalGetWithoutIsPresent // .entity is required // invalid data should fail
        return EntityType.getEntityFromTag(entityTag, world).get();
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = entity.world;
        Entity held = getEntity(stack, world);

        Entity vehicle = entity;
        while (vehicle.getPrimaryPassenger() != null) {
            vehicle = vehicle.getPrimaryPassenger();
        }

        if (!vehicle.hasPassengers() && held.startRiding(vehicle, true)) {
            entity.updatePassengerPosition(held);
            world.spawnEntity(held);

            user.getStackInHand(hand).setCount(0);

            return ActionResult.success(world.isClient);
        }

        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usage) {
        World world = usage.getWorld();
        ItemStack stack = usage.getStack();
        ItemPlacementContext placement = new ItemPlacementContext(usage);

        if (placement.canPlace()) {
            Vec3d pos = placement.getHitPos();

            Entity entity = getEntity(stack, world);

            entity.updatePosition(pos.getX(), pos.getY(), pos.getZ());
            world.spawnEntity(entity);

            //TODO: handle this?
            usage.getPlayer().getStackInHand(usage.getHand()).setCount(0);

            return ActionResult.success(world.isClient);
        }
        return super.useOnBlock(usage);
    }

    @Override
    public String getHeldTranslationKey(ItemStack stack) {
        CompoundTag entityTag = stack.getSubTag("entity");
        if (entityTag == null) {
            return META.INVALID_KEY;
        }
        return EntityType.fromTag(entityTag)
            .map(EntityType::getTranslationKey)
            .orElse(META.INVALID_KEY);
    }
}
