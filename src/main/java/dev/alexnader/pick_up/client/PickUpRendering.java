package dev.alexnader.pick_up.client;

import dev.alexnader.pick_up.mixin.BlockEntityAccess;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Random;

public class PickUpRendering {
    private static final ThreadLocal<BlockRenderContext> CONTEXTS = ThreadLocal.withInitial(BlockRenderContext::new);

    public static void renderHeldBlock(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();

        CompoundTag tag = stack.getOrCreateTag();
        //noinspection OptionalGetWithoutIsPresent // should fail if this tag is missing or invalid
        BlockState state = BlockState.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("state")).get().left().get().getFirst();

        BlockEntity entity = BlockEntity.createFromTag(state, tag.getCompound("entity"));
        //noinspection ConstantConditions // should always be a BE
        BlockPos pos = entity.getPos();
        entity.setLocation(client.world, pos);

        ((BlockEntityAccess) entity).setCachedState(state);

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

        CONTEXTS.get().render(
            new BlockEntityDelegateWorld(client.world, pos, state, entity),
            client.getBlockRenderManager().getModel(state),
            state,
            pos,
            matrices,
            vertexConsumer.getBuffer(RenderLayers.getEntityBlockLayer(state, false)),
            new Random(),
            0,
            overlay
        );

        renderBer(entity, matrices, vertexConsumer, light, overlay);

        matrices.pop();
    }

    private static <E extends BlockEntity> void renderBer(E entity, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
        @Nullable BlockEntityRenderer<E> ber = BlockEntityRenderDispatcher.INSTANCE.get(entity);
        if (ber != null) {
            ber.render(entity, MinecraftClient.getInstance().getTickDelta(), matrices, vertexConsumer, light, overlay);
        }
    }

    public static void renderHeldEntity(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();

        CompoundTag tag = stack.getOrCreateTag();
        //noinspection OptionalGetWithoutIsPresent // should fail if this tag is invalid
        Entity entity = EntityType.getEntityFromTag(tag.getCompound("entity"), client.world).get();
        entity.prevYaw = 0;
        entity.yaw = 0;
        entity.prevPitch = 0;
        entity.pitch = 0;
        entity.resetPosition(0, 0, 0);
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.prevBodyYaw = 0;
            living.bodyYaw = 0;
            living.prevHeadYaw = 0;
            living.headYaw = 0;
            living.hurtTime = 0;
            living.deathTime = 0;
        }

        matrices.push();

        if (mode == ModelTransformation.Mode.GUI) {
            matrices.translate(0.5, 0, 0);
        }

        if (mode.isFirstPerson()) {
            matrices.translate(0, -entity.getBoundingBox().getYLength() / 2f + 0.25, 0);
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(8));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
        }

        client.getEntityRenderDispatcher()
            .getRenderer(entity)
            .render(entity, 0, client.getTickDelta(), matrices, vertexConsumer, light);

        matrices.pop();
    }
}
