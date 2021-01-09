package dev.alexnader.pick_up.mixin.server;

import dev.alexnader.pick_up.common.Denylist;
import dev.alexnader.pick_up.mixinterface.ServerDenylist;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ServerDenylist {
    @Shadow
    private PlayerManager playerManager;

    @Unique
    private final Denylist denylist = Denylist.fromConfig();

    @Override
    public Denylist denylist() {
        return denylist;
    }

    @Override
    public void sendDenylistToPlayers() {
        playerManager.getPlayerList().forEach(denylist::sendToPlayer);
    }
}
