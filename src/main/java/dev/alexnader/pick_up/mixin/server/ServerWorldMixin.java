package dev.alexnader.pick_up.mixin.server;

import dev.alexnader.pick_up.common.Denylist;
import dev.alexnader.pick_up.mixinterface.DenylistSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements DenylistSource {
    @Shadow
    private @Final MinecraftServer server;

    @Override
    public Denylist denylist() {
        return ((DenylistSource) server).denylist();
    }
}
