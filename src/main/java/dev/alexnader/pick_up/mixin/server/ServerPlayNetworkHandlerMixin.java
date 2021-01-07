package dev.alexnader.pick_up.mixin.server;

import dev.alexnader.pick_up.common.event.SwapHandsCallback;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(
        method = "onPlayerAction",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;",
            ordinal = 0),
        cancellable = true
    )
    void fireSwapHandsCallback(PlayerActionC2SPacket packet, CallbackInfo ci) {
        ActionResult result = SwapHandsCallback.EVENT.invoker()
            .swap(player, player.getStackInHand(Hand.MAIN_HAND), player.getStackInHand(Hand.OFF_HAND));

        if (result != ActionResult.PASS) {
            ci.cancel();
        }
    }
}
