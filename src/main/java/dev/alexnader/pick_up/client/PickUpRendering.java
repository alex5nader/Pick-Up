package dev.alexnader.pick_up.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import javax.annotation.Nullable;

public class PickUpRendering {
    public static void renderHeldBlock(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();

        CompoundTag tag = stack.getOrCreateTag();
        //noinspection OptionalGetWithoutIsPresent // should fail if this tag is missing or invalid
        BlockState state = BlockState.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("state")).get().left().get().getFirst();
        BlockEntity entity = BlockEntity.createFromTag(state, tag.getCompound("entity"));

        matrices.push();

        if (mode.isFirstPerson()) {
            if (mode == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND) {
                matrices.translate(-0.5, -0.15, 0);
            } else {
                matrices.translate(0.6125, -0.15, 0);
            }
            matrices.scale(0.9f, 0.9f, 0.9f);
            matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(8));
        }

        client.getBlockRenderManager().renderBlockAsEntity(state, matrices, vertexConsumer, light, overlay);
        renderBer(entity, matrices, vertexConsumer, light, overlay);

        matrices.pop();
    }

    private static <E extends BlockEntity> void renderBer(E entity, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
        @Nullable BlockEntityRenderer<E> ber = BlockEntityRenderDispatcher.INSTANCE.get(entity);
        if (ber != null) {
            ber.render(entity, MinecraftClient.getInstance().getTickDelta(), matrices, vertexConsumer, light, overlay);
        }
    }
}
