package dev.alexnader.pick_up.mixin.client;

import dev.alexnader.pick_up.common.event.SelectSlotCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryClientMixin {
    @Shadow
    public int selectedSlot;
    @Shadow
    public @Final PlayerEntity player;

    @Unique
    private int savedSlot;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"))
    void storeOriginalSlot(double scrollAmount, CallbackInfo ci) {
        savedSlot = selectedSlot;
    }

    @Inject(method = "scrollInHotbar", at = @At("TAIL"))
    void fireSelectSlotCallback(double scrollAmount, CallbackInfo ci) {
        int newSlot = this.selectedSlot;
        this.selectedSlot = savedSlot;
        ActionResult result = SelectSlotCallback.EVENT.invoker().selectSlot(player, newSlot);

        if (result == ActionResult.PASS) {
            selectedSlot = newSlot;
        }
    }
}
