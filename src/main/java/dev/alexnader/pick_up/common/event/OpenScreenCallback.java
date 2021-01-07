package dev.alexnader.pick_up.common.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface OpenScreenCallback {
    Event<OpenScreenCallback> BEFORE_OPEN = EventFactory.createArrayBacked(
        OpenScreenCallback.class,
        (listeners) -> (player) -> {
            for (OpenScreenCallback listener : listeners) {
                ActionResult result = listener.open(player);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult open(PlayerEntity player);
}
