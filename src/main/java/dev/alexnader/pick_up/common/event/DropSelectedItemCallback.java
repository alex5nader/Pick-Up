package dev.alexnader.pick_up.common.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface DropSelectedItemCallback {
    Event<DropSelectedItemCallback> EVENT = EventFactory.createArrayBacked(
        DropSelectedItemCallback.class,
        (listeners) -> (player, stack, dropEntireStack) -> {
            for (DropSelectedItemCallback listener : listeners) {
                ActionResult result = listener.drop(player, stack, dropEntireStack);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult drop(PlayerEntity player, ItemStack stack, boolean dropEntireStack);
}
