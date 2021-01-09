package dev.alexnader.pick_up.client;

import dev.alexnader.pick_up.common.Denylist;
import dev.alexnader.pick_up.mixinterface.RemoteDenylist;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;

public class PickUpClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(ITEMS.HELD_BLOCK_ITEM.value, PickUpRendering::renderHeldBlock);
        BuiltinItemRendererRegistry.INSTANCE.register(ITEMS.HELD_ENTITY_ITEM.value, PickUpRendering::renderHeldEntity);

        ClientPlayNetworking.registerGlobalReceiver(
            Denylist.PACKET_ID,
            (client, handler, buf, responseSender) -> ((RemoteDenylist) client).receiveDenylist(buf)
        );
    }
}
