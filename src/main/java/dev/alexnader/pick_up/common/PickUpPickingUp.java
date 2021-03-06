package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import dev.alexnader.pick_up.common.item.HeldEntityItem;
import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import dev.alexnader.server_config.api.ConfigProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PickUpPickingUp {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // inverted name is harder to read
    private static boolean canPickUp(PlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).isEmpty()
            && player.getStackInHand(Hand.OFF_HAND).isEmpty()
            && player.isSneaking();
    }

    public static ActionResult tryPickUpEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hit) {
        if (!canPickUp(player) || ((ConfigProvider) world).config(PickUpConfig.ENTITY_TYPE_DENYLIST).isDenied(entity.getType())) {
            return ActionResult.PASS;
        }

        if (world.isClient) {
            //noinspection ConstantConditions // if this fails, FAPI is also broken
            Vec3d hitVec = hit.getPos().subtract(entity.getX(), entity.getY(), entity.getZ());
            //noinspection ConstantConditions // this event is only fired while in a world, so networkHandler will be present
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractEntityC2SPacket(entity, hand, hitVec, player.isSneaking()));
            return ActionResult.CONSUME;
        }

        player.setStackInHand(Hand.MAIN_HAND, HeldEntityItem.stackHolding(entity));
        entity.remove();

        return ActionResult.CONSUME;
    }

    public static ActionResult tryPickUpBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        // picking up every block would allow moving bedrock, etc // TODO configurable?
        if (!state.getBlock().hasBlockEntity()) {
            return ActionResult.PASS;
        }

        if (!canPickUp(player) || ((ConfigProvider) world).config(PickUpConfig.BLOCK_DENYLIST).isDenied(state.getBlock())) {
            return ActionResult.PASS;
        }
        if (!((BlockBreakRestrictable) player).canBreakBlock(world, pos)) {
            return ActionResult.PASS;
        }

        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof LockableContainerBlockEntity && !((LockableContainerBlockEntity) entity).checkUnlocked(player)) {
            return ActionResult.PASS;
        }

        // should be a valid pick up, let server tell client what happens
        if (world.isClient) {
            // FAPI will only send the packet on SUCCESS, but the event still needs to be fired on the server
            // noinspection ConstantConditions // networkHandler will not be null when a block is interacted with
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hit));
            return ActionResult.CONSUME;
        }

        //noinspection ConstantConditions // should not be null
        player.setStackInHand(Hand.MAIN_HAND, HeldBlockItem.stackHolding(state, entity));
        world.removeBlockEntity(pos);
        world.removeBlock(pos, true);

        System.out.println("pick up!");
        return ActionResult.CONSUME;
    }
}
