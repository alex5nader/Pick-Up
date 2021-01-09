package dev.alexnader.pick_up.mixin.client;

import dev.alexnader.pick_up.common.Denylist;
import dev.alexnader.pick_up.mixinterface.DenylistSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements DenylistSource {
    @Shadow
    private @Final MinecraftClient client;

    @Override
    public Denylist denylist() {
        return ((DenylistSource) client).denylist();
    }
}
