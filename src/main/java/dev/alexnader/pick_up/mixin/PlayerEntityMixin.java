package dev.alexnader.pick_up.mixin;

import dev.alexnader.pick_up.common.event.DropSelectedItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Shadow @Final public PlayerInventory inventory;

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    void fireDropSelectedItemCallback(boolean dropEntireStack, CallbackInfoReturnable<Boolean> cir) {
        ActionResult result = DropSelectedItemCallback.EVENT.invoker().drop((PlayerEntity) (Object) this, this.inventory.getStack(this.inventory.selectedSlot), dropEntireStack);

        if (result != ActionResult.PASS) {
            cir.setReturnValue(false);
        }
    }
}
