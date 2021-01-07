package dev.alexnader.pick_up.common.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface SelectSlotCallback {
    Event<SelectSlotCallback> EVENT = EventFactory.createArrayBacked(SelectSlotCallback.class,
        (listeners) -> (player, slot) -> {
            for (SelectSlotCallback listener : listeners) {
                ActionResult result = listener.selectSlot(player, slot);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult selectSlot(PlayerEntity player, int slot);
}
