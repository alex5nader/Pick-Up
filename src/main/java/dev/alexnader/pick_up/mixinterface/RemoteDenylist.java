package dev.alexnader.pick_up.mixinterface;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public interface RemoteDenylist extends DenylistSource {
    void receiveDenylist(PacketByteBuf buf);
}
