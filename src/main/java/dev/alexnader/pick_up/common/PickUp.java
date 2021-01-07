package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.api.PickUpDenylist;
import dev.alexnader.pick_up.api.PickUpEntrypoint;
import dev.alexnader.pick_up.common.event.DropSelectedItemCallback;
import dev.alexnader.pick_up.common.event.OpenScreenCallback;
import dev.alexnader.pick_up.common.event.SelectSlotCallback;
import dev.alexnader.pick_up.common.event.SwapHandsCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;
    public static PickUpCommands COMMANDS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();
        COMMANDS = new PickUpCommands();

        for (PickUpEntrypoint entrypoint : FabricLoader.getInstance()
            .getEntrypoints(META.NAMESPACE, PickUpEntrypoint.class)) {
            entrypoint.doPickUpRelatedThings();
        }

        initDenylist();
        registerInteractionBlockers();

        CommandRegistrationCallback.EVENT.register((dispatcher, isDedi) -> dispatcher.register(COMMANDS.DISCARD));

        UseEntityCallback.EVENT.register(PickUpPickingUp::tryPickUpEntity);
        UseBlockCallback.EVENT.register(PickUpPickingUp::tryPickUpBlock);
    }

    private void initDenylist() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            if (!meta.containsCustomValue(META.NAMESPACE)) {
                continue;
            }

            CustomValue.CvObject pickUpData = meta.getCustomValue(META.NAMESPACE).getAsObject();

            if (pickUpData.containsKey("denied_blocks")) {
                for (CustomValue deniedBlock : pickUpData.get("denied_blocks").getAsArray()) {
                    PickUpDenylist.INSTANCE.deny(Registry.BLOCK.get(new Identifier(deniedBlock.getAsString())));
                }
            }

            if (pickUpData.containsKey("denied_entities")) {
                for (CustomValue deniedEntity : pickUpData.get("denied_entities").getAsArray()) {
                    PickUpDenylist.INSTANCE.deny(Registry.ENTITY_TYPE.get(new Identifier(deniedEntity.getAsString())));
                }
            }
        }
    }

    private void registerInteractionBlockers() {
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

        SelectSlotCallback.EVENT.register((player, slot) -> {
            if (ITEMS.isHeldItem(player.getStackInHand(Hand.MAIN_HAND).getItem())) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });

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
