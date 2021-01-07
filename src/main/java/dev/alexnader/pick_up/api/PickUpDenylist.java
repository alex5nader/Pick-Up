package dev.alexnader.pick_up.api;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Set;

public enum PickUpDenylist {
    INSTANCE;

    private final Set<EntityType<?>> deniedEntityTypes = new ReferenceOpenHashSet<>();
    private final Set<Block> deniedBlocks = new ReferenceOpenHashSet<>();

    public void deny(EntityType<?> entityType) {
        deniedEntityTypes.add(entityType);
    }

    public void deny(Block block) {
        deniedBlocks.add(block);
    }

    public boolean isDenied(EntityType<?> entityType) {
        return deniedEntityTypes.contains(entityType);
    }

    public boolean isDenied(Entity entity) {
        return isDenied(entity.getType());
    }

    public boolean isDenied(Block block) {
        return deniedBlocks.contains(block);
    }

    public boolean isDenied(BlockState state) {
        return isDenied(state.getBlock());
    }
}
