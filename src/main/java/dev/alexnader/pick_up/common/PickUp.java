package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            BlockState state = world.getBlockState(hit.getBlockPos());
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
            if (!((BlockBreakRestrictable) player).canBreakBlock(world, hit.getBlockPos())) {
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
            player.setStackInHand(Hand.MAIN_HAND, HeldBlockItem.stackHolding(state, world.getBlockEntity(hit.getBlockPos())));
            world.removeBlockEntity(hit.getBlockPos());
            world.removeBlock(hit.getBlockPos(), true);

            System.out.println("pick up!");
            return ActionResult.CONSUME;
        });
    }
}
