package dev.alexnader.pick_up.common;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.alexnader.pick_up.common.item.HeldItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;
import static dev.alexnader.pick_up.common.PickUpMeta.id;
import static net.minecraft.server.command.CommandManager.literal;

public class PickUpCommands {
    public LiteralArgumentBuilder<ServerCommandSource> DISCARD =
        literal(id("discard").toString())
            .executes(PickUpCommands::discard);

    private static int discard(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
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
    }
}
