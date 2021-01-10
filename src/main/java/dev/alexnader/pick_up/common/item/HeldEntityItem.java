package dev.alexnader.pick_up.common.item;

import dev.alexnader.pick_up.common.PickUpMeta;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;
import static dev.alexnader.pick_up.common.util.Util.toVec3d;

public class HeldEntityItem extends HeldItem {
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

            user.inventory.removeStack(user.inventory.selectedSlot);

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
            Vec3d pos = toVec3d(placement.getBlockPos()).add(0.5, 0, 0.5);

            Entity entity = getEntity(stack, world);
            entity.updatePosition(pos.getX(), pos.getY(), pos.getZ());

            if (entity.isInsideWall()) {
                return ActionResult.FAIL;
            }

            world.spawnEntity(entity);

            PlayerEntity user = usage.getPlayer();
            if (user != null) {
                user.inventory.removeStack(user.inventory.selectedSlot);
            }

            return ActionResult.success(world.isClient);
        }
        return super.useOnBlock(usage);
    }

    @Override
    public String getHeldTranslationKey(ItemStack stack) {
        CompoundTag entityTag = stack.getSubTag("entity");
        if (entityTag == null) {
            return PickUpMeta.INVALID_KEY;
        }
        return EntityType.fromTag(entityTag)
            .map(EntityType::getTranslationKey)
            .orElse(PickUpMeta.INVALID_KEY);
    }
}
