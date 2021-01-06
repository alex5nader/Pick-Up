package dev.alexnader.pick_up.mixinterface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockBreakRestrictable {
    boolean canBreakBlock(World world, BlockPos pos);
}
