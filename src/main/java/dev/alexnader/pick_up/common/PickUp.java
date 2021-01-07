package dev.alexnader.pick_up.common;

import dev.alexnader.pick_up.common.item.HeldItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

import static net.minecraft.server.command.CommandManager.literal;

public class PickUp implements ModInitializer {
    public static PickUpMeta META;
    public static PickUpItems ITEMS;

    @Override
    public void onInitialize() {
        META = new PickUpMeta();
        ITEMS = new PickUpItems();

        //noinspection CodeBlock2Expr // more readable with explicit block
        CommandRegistrationCallback.EVENT.register((dispatcher, isDedi) -> {
            dispatcher.register(literal(META.id("discard").toString()).executes(ctx -> {
                ServerCommandSource source = ctx.getSource();
                PlayerEntity sender = source.getPlayer();
                ItemStack stack = sender.getStackInHand(Hand.MAIN_HAND);

                if (!ITEMS.isHeldItem(stack.getItem())) {
                    source.sendError(new TranslatableText("pick_up.command.discard.not_holding"));
                    return -1;
                }

                source.sendFeedback(
                    new TranslatableText("pick_up.command.discard.success")
                        .append(new TranslatableText(((HeldItem) stack.getItem()).getHeldTranslationKey(stack))),
                    false
                );
                stack.setCount(0);
                return 1;
            }));
        });

        UseEntityCallback.EVENT.register(PickUpPickingUp::tryPickUpEntity);
        UseBlockCallback.EVENT.register(PickUpPickingUp::tryPickUpBlock);
    }
}
