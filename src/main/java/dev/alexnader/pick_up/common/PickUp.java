package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.event.DropSelectedItemCallback;
import dev.alexnader.pick_up.common.event.OpenScreenCallback;
import dev.alexnader.pick_up.common.event.SwapHandsCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;
    public static PickUpCommands COMMANDS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();
        COMMANDS = new PickUpCommands();

        DropSelectedItemCallback.EVENT.register((player, stack, dropEntireStack) -> {
            if (ITEMS.isHeldItem(stack.getItem())) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });

        OpenScreenCallback.BEFORE_OPEN.register((player) -> {
            if (ITEMS.isHeldItem(player.getStackInHand(Hand.MAIN_HAND).getItem())) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });

        SwapHandsCallback.EVENT.register((player, mainHandStack, offHandStack) -> {
            if (ITEMS.isHeldItem(mainHandStack.getItem())) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });

        //noinspection CodeBlock2Expr // more readable with explicit block
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedi) -> {
            dispatcher.register(COMMANDS.DISCARD);
        });

        UseEntityCallback.EVENT.register(PickUpPickingUp::tryPickUpEntity);
        UseBlockCallback.EVENT.register(PickUpPickingUp::tryPickUpBlock);

        // prevent swinging hand for block GUIs
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!ITEMS.isHeldItem(player.getStackInHand(hand).getItem())) {
                return ActionResult.PASS;
            }

            BlockState block = world.getBlockState(hitResult.getBlockPos());
            if (block.createScreenHandlerFactory(world, hitResult.getBlockPos()) == null) {
                return ActionResult.PASS;
            }

            if (player.isSneaking()) {
                return ActionResult.PASS;
            }

            return ActionResult.CONSUME;
        });
    }
}
