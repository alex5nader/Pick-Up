package dev.alexnader.pick_up.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}
