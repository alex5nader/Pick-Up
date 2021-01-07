package dev.alexnader.pick_up.client;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

import javax.annotation.Nullable;

/**
 * This class is used to provide a (somewhat) functional {@link RenderAttachedBlockView}
 * for rendering block entities that aren't actually in a world.
 */
public class BlockEntityDelegateWorld implements RenderAttachedBlockView {
    private final BlockRenderView realWorld;
    private final BlockPos pos;
    private final BlockState state;
    private final BlockEntity entity;

    public BlockEntityDelegateWorld(BlockRenderView realWorld, BlockPos pos, BlockState state, BlockEntity entity) {
        this.realWorld = realWorld;
        this.pos = pos;
        this.state = state;
        this.entity = entity;
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return realWorld.getBrightness(direction, shaded);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return realWorld.getLightingProvider();
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return realWorld.getColor(pos, colorResolver);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        if (this.pos.equals(pos)) {
            return entity;
        } else {
            return realWorld.getBlockEntity(pos);
        }
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.pos.equals(pos)) {
            return state;
        } else {
            return realWorld.getBlockState(pos);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return realWorld.getFluidState(pos);
    }
}
