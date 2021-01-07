package dev.alexnader.pick_up.mixin.client;

import dev.alexnader.pick_up.common.event.OpenScreenCallback;
import dev.alexnader.pick_up.common.event.SwapHandsCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.network.Packet;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Unique
    private ActionResult openScreenCallbackResult = ActionResult.PASS;

    @Redirect(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/options/GameOptions;keySwapHands:Lnet/minecraft/client/options/KeyBinding;"))
    )
     void fireSwapHandsCallback(ClientPlayNetworkHandler clientPlayNetworkHandler, Packet<?> packet) {
        //noinspection ConstantConditions // should not be null when handling input events
        ActionResult result = SwapHandsCallback.EVENT.invoker().swap(player, player.getStackInHand(Hand.MAIN_HAND), player.getStackInHand(Hand.OFF_HAND));

        if (result == ActionResult.PASS) {
            clientPlayNetworkHandler.sendPacket(packet);
        }
    }

    @Inject(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasRidingInventory()Z", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/options/GameOptions;keyInventory:Lnet/minecraft/client/options/KeyBinding;"))
    )
    void fireOpenScreenCallback(CallbackInfo ci) {
        openScreenCallbackResult = OpenScreenCallback.BEFORE_OPEN.invoker().open(player);
    }

    @Redirect(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;openRidingInventory()V", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/options/GameOptions;keyInventory:Lnet/minecraft/client/options/KeyBinding;"))
    )
    void blockOpenRidingInventoryUnlessOpenScreenCallbackPasses(ClientPlayerEntity player) {
        if (openScreenCallbackResult == ActionResult.PASS) {
            player.openRidingInventory();
        }
    }

    @Redirect(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onInventoryOpened()V", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/options/GameOptions;keyInventory:Lnet/minecraft/client/options/KeyBinding;"))
    )
    void blockOnInventoryOpenedUnlessOpenScreenCallbackPasses(TutorialManager tutorialManager) {
        if (openScreenCallbackResult == ActionResult.PASS) {
            tutorialManager.onInventoryOpened();
        }
    }

    @Redirect(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/options/GameOptions;keyInventory:Lnet/minecraft/client/options/KeyBinding;"))
    )
    void blockOpenScreenUnlessOpenScreenCallbackPasses(MinecraftClient client, Screen screen) {
        if (openScreenCallbackResult == ActionResult.PASS) {
            client.openScreen(screen);
        }
    }
}
