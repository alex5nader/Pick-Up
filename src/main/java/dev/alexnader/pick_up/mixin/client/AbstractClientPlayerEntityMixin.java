package dev.alexnader.pick_up.mixin.client;

import dev.alexnader.pick_up.mixinterface.BlockBreakRestrictable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayerEntity.class)
@Environment(EnvType.CLIENT)
public abstract class AbstractClientPlayerEntityMixin implements BlockBreakRestrictable {
    @Override
    public boolean canBreakBlock(World world, BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();

        //noinspection ConstantConditions // neither player nor interactionManager can be null while in-world
        return !client.player.isBlockBreakingRestricted(world, pos, client.interactionManager.getCurrentGameMode());
    }
}
