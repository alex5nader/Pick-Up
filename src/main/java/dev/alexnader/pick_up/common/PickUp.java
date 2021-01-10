package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.event.DropSelectedItemCallback;
import dev.alexnader.pick_up.common.event.OpenScreenCallback;
import dev.alexnader.pick_up.common.event.SelectSlotCallback;
import dev.alexnader.pick_up.common.event.SwapHandsCallback;
import dev.alexnader.server_config.api.ConfigKey;
import dev.alexnader.server_config.api.ServerConfigProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PickUp implements ModInitializer {
    public static PickUpItems ITEMS;
    public static PickUpCommands COMMANDS;

    @Override
    public void onInitialize() {
        ITEMS = new PickUpItems();
        COMMANDS = new PickUpCommands();

        registerInteractionBlockers();

        CommandRegistrationCallback.EVENT.register((dispatcher, isDedi) -> dispatcher.register(COMMANDS.DISCARD));

        ServerLifecycleEvents.SERVER_STARTING.register(PickUp::loadMetadataDenylists);

        UseEntityCallback.EVENT.register(PickUpPickingUp::tryPickUpEntity);
        UseBlockCallback.EVENT.register(PickUpPickingUp::tryPickUpBlock);
    }

    private static void loadMetadataDenylists(MinecraftServer server) {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            if (!meta.containsCustomValue(PickUpMeta.NAMESPACE)) {
                continue;
            }

            CustomValue.CvObject pickUpData = meta.getCustomValue(PickUpMeta.NAMESPACE).getAsObject();
            ServerConfigProvider configs = (ServerConfigProvider) server;

            readMetadata(pickUpData, configs, PickUpConfig.BLOCK_DENYLIST, Registry.BLOCK, "denied_blocks");
            readMetadata(pickUpData, configs, PickUpConfig.ENTITY_TYPE_DENYLIST, Registry.ENTITY_TYPE, "denied_entities");
        }
    }

    private static <T> void readMetadata(CustomValue.CvObject obj, ServerConfigProvider configs, ConfigKey<Denylist<T>> configKey, Registry<T> registry, String metadataKey) {
        Denylist<T> denylist = configs.config(configKey);

        if (obj.containsKey(metadataKey)) {
            for (CustomValue deniedBlock : obj.get(metadataKey).getAsArray()) {
                denylist.deny(registry.get(new Identifier(deniedBlock.getAsString())));
            }
        }

        configs.save(configKey);
        configs.syncToAll(configKey);
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
