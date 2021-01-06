package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            // picking up every block would allow moving bedrock, etc
            if (!state.getBlock().hasBlockEntity()) {
                return ActionResult.PASS;
            }

            if (!player.getStackInHand(Hand.MAIN_HAND).isEmpty() || !player.getStackInHand(Hand.OFF_HAND).isEmpty()) {
                return ActionResult.PASS;
            }
            if (!player.isSneaking()) {
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
        });
    }
}