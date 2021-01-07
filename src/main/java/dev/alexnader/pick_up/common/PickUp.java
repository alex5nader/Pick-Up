package dev.alexnader.pick_up.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;
    public static PickUpCommands COMMANDS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();
        COMMANDS = new PickUpCommands();

        //noinspection CodeBlock2Expr // more readable with explicit block
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedi) -> {
            dispatcher.register(COMMANDS.DISCARD);
        });

        UseEntityCallback.EVENT.register(PickUpPickingUp::tryPickUpEntity);
        UseBlockCallback.EVENT.register(PickUpPickingUp::tryPickUpBlock);
    }
}
