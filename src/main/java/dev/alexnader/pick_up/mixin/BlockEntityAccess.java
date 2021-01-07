package dev.alexnader.pick_up.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public interface BlockEntityAccess {
    @Accessor("cachedState")
    void setCachedState(@Nullable BlockState state);
}
