package dev.alexnader.pick_up.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;

public class HeldBlockItem extends Item {
    public HeldBlockItem(Settings settings) {
        super(settings);
    }

    public static BlockState getState(ItemStack stack) {
        //noinspection OptionalGetWithoutIsPresent
        return BlockState.CODEC.decode(NbtOps.INSTANCE, stack.getSubTag("state")).get().left().get().getFirst();
    }

    public static ItemStack stackHolding(BlockState state, BlockEntity entity) {
        ItemStack stack = new ItemStack(ITEMS.HELD_BLOCK_ITEM.value);
        CompoundTag tag = stack.getOrCreateTag();
        //noinspection OptionalGetWithoutIsPresent // should fail if BlockState is invalid
        tag.put("state", BlockState.CODEC.encode(state, NbtOps.INSTANCE, new CompoundTag()).get().left().get());
        tag.put("entity", entity.toTag(new CompoundTag()));
        return stack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usage) {
        World world = usage.getWorld();
        ItemStack stack = usage.getStack();
        ItemPlacementContext placement = new ItemPlacementContext(usage);

        if (placement.canPlace()) {
            BlockState state = getState(stack);
            BlockItem item = (BlockItem) state.getBlock().asItem();

            ActionResult result = item.place(placement);
            if (result.isAccepted()) {
                ItemPlacementContext itemCtx = item.getPlacementContext(placement);
                //noinspection ConstantConditions // itemCtx should be non-null if result is accepted
                BlockPos pos = itemCtx.getBlockPos();
                BlockEntity entity = world.getBlockEntity(pos);
                CompoundTag entityTag = stack.getSubTag("entity");
                //noinspection ConstantConditions // .entity is required
                entityTag.putInt("x", pos.getX());
                entityTag.putInt("y", pos.getY());
                entityTag.putInt("z", pos.getZ());
                //noinspection ConstantConditions // entity should be non-null because HeldBlockItem always holds block entities
                entity.fromTag(world.getBlockState(pos), entityTag);
            }

            //TODO: handle this?
            usage.getPlayer().getStackInHand(usage.getHand()).setCount(0);

            return result;
        }
        return super.useOnBlock(usage);
    }

    @Override
    public Text getName() {
        return super.getName();
    }
}
