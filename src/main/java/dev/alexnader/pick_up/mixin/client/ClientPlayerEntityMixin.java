package dev.alexnader.pick_up.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.alexnader.pick_up.common.event.DropSelectedItemCallback;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
        throw new IllegalStateException("Mixin constructor should not run.");
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    void fireDropSelectedItemCallback(boolean dropEntireStack, CallbackInfoReturnable<Boolean> cir) {
        ActionResult result = DropSelectedItemCallback.EVENT.invoker().drop(this, this.inventory.getStack(this.inventory.selectedSlot), dropEntireStack);

        if (result != ActionResult.PASS) {
            cir.setReturnValue(false);
        }
    }
}
