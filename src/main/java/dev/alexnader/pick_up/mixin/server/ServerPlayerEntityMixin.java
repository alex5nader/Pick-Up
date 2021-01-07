package dev.alexnader.pick_up.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.alexnader.pick_up.common.event.OpenScreenCallback;
import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements BlockBreakRestrictable {
    @Shadow @Final public ServerPlayerInteractionManager interactionManager;

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
        throw new IllegalStateException("Mixin constructor should not run.");
    }

    @Override
    public boolean canBreakBlock(World world, BlockPos pos) {
        return !this.isBlockBreakingRestricted(world, pos, interactionManager.getGameMode());
    }

    @Inject(
        method = "openHandledScreen",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/NamedScreenHandlerFactory;createMenu(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/screen/ScreenHandler;"),
        cancellable = true
    )
    void fireOpenScreenCallback(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        ActionResult result = OpenScreenCallback.BEFORE_OPEN.invoker().open(this);

        if (result != ActionResult.PASS) {
            cir.setReturnValue(OptionalInt.empty());
        }
    }
}
