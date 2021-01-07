package dev.alexnader.pick_up.common.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface SwapHandsCallback {
    Event<SwapHandsCallback> EVENT = EventFactory.createArrayBacked(
        SwapHandsCallback.class,
        (listeners) -> (player, mainHandStack, offHandStack) -> {
            for (SwapHandsCallback listener : listeners) {
                ActionResult result = listener.swap(player, mainHandStack, offHandStack);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult swap(PlayerEntity player, ItemStack mainHandStack, ItemStack offHandStack);
}
