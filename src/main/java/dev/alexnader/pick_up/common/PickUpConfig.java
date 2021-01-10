package dev.alexnader.pick_up.common;

import dev.alexnader.server_config.api.ConfigKey;
import dev.alexnader.server_config.api.ServerConfigEntrypoint;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

import static dev.alexnader.pick_up.common.PickUpMeta.id;

public class PickUpConfig implements ServerConfigEntrypoint {
    public static final ConfigKey<Denylist<Block>> BLOCK_DENYLIST = new ConfigKey<>(id("denied_blocks.txt"), () -> new Denylist<>(Registry.BLOCK));
    public static final ConfigKey<Denylist<EntityType<?>>> ENTITY_TYPE_DENYLIST = new ConfigKey<>(id("denied_entities.txt"), () -> new Denylist<>(Registry.ENTITY_TYPE));

    @Override
    public void registerConfigs(Consumer<ConfigKey<?>> consumer) {
        consumer.accept(BLOCK_DENYLIST);
        consumer.accept(ENTITY_TYPE_DENYLIST);
    }
}
