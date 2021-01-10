package dev.alexnader.pick_up.common.item;

import dev.alexnader.pick_up.common.PickUpConfig;
import dev.alexnader.pick_up.common.PickUpMeta;
import dev.alexnader.server_config.api.ServerConfigProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Objects;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;
import static dev.alexnader.pick_up.common.PickUpMeta.LOGGER;
import static dev.alexnader.pick_up.common.util.Util.linkTo;

public class HeldBlockItem extends HeldItem {
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
        System.out.println("useOnBlock");
        World world = usage.getWorld();
        ItemStack stack = usage.getStack();
        ItemPlacementContext placement = new ItemPlacementContext(usage);

        if (!placement.canPlace()) {
            return super.useOnBlock(usage);
        }

        PlayerEntity user = usage.getPlayer();
        if (user != null) {
            user.inventory.removeStack(user.inventory.selectedSlot);
        }

        BlockState state = getState(stack);

        Item cachedItem = state.getBlock().asItem();

        // ideal case, everything is good
        if (cachedItem instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) cachedItem;
            ActionResult result = blockItem.place(placement);
            if (result.isAccepted()) {
                ItemPlacementContext itemCtx = blockItem.getPlacementContext(placement);
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

            return result;
        }

        // something is wrong
        if (!world.isClient) {
            ServerConfigProvider server = (ServerConfigProvider) ((ServerWorld) world).getServer();
            server.config(PickUpConfig.BLOCK_DENYLIST).deny(state.getBlock());
            server.save(PickUpConfig.BLOCK_DENYLIST);
            server.syncToAll(PickUpConfig.BLOCK_DENYLIST);

            if (user != null) {
                user.sendMessage(
                    new TranslatableText("pick_up.message.error.placing")
                        .setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                    false
                );
                user.sendMessage(linkTo("https://github.com/alex5nader/Pick-Up/issues/new"), false);
            }
        }

        // still have an item, just try to use it
        if (cachedItem != null) {
            LOGGER.warn("`{}`'s item, `{}`, is not a proper BlockItem. Denylisting it.", Registry.BLOCK.getId(state.getBlock()), Registry.ITEM.getId(cachedItem));

            ActionResult result = cachedItem.useOnBlock(usage);

            BlockPos pos = placement.getBlockPos();
            BlockState oldState = world.getBlockState(pos);

            // useOnBlock may have not actually placed something
            if (!Objects.equals(world.getBlockState(pos), oldState)) {
                CompoundTag entityTag = stack.getSubTag("entity");
                //noinspection ConstantConditions // .entity is required
                entityTag.putInt("x", pos.getX());
                entityTag.putInt("y", pos.getY());
                entityTag.putInt("z", pos.getZ());
                BlockEntity entity = BlockEntity.createFromTag(state, entityTag);
                world.setBlockEntity(pos, entity);

                return result;
            }
        }

        // everything is broken, just place the BlockState and BlockEntity directly.
        if (cachedItem == null) {
            LOGGER.warn("Failed to get an item for `{}`. Denylisting it.", Registry.BLOCK.getId(state.getBlock()));
        }

        BlockPos pos = placement.getBlockPos();
        world.setBlockState(pos, state);

        CompoundTag entityTag = stack.getSubTag("entity");
        //noinspection ConstantConditions // .entity is required
        entityTag.putInt("x", pos.getX());
        entityTag.putInt("y", pos.getY());
        entityTag.putInt("z", pos.getZ());
        BlockEntity entity = world.getBlockEntity(pos);
        //noinspection ConstantConditions // entity should be non-null because HeldBlockItem always holds block entities
        entity.fromTag(state, entityTag);

        return ActionResult.success(world.isClient);
    }

    @Override
    public Text getName() {
        return super.getName();
    }

    @Override
    public String getHeldTranslationKey(ItemStack stack) {
        CompoundTag stateTag = stack.getSubTag("state");
        if (stateTag == null) {
            return PickUpMeta.INVALID_KEY;
        }
        String name = stateTag.getString("Name");
        if ("".equals(name)) {
            return PickUpMeta.INVALID_KEY;
        }
        return Registry.BLOCK.get(new Identifier(name)).getTranslationKey();
    }
}
