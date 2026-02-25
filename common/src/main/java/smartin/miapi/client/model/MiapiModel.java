package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * default model to implement Module level Models.
 * Should be registered at {@link MiapiItemModel#modelSuppliers}
 */
@Environment(EnvType.CLIENT)
public interface MiapiModel {

    void render(RenderContext context);

    default Matrix4f subModuleMatrix(RenderContext context) {
        return new Matrix4f();
    }

    default boolean hasAnimatedModuleMatrix() {
        return false;
    }

    record RenderContext(@Nullable String modelType,
                         PoseStack matrices,
                         Matrix4f objectSpace,
                         ItemStack stack,
                         ItemDisplayContext transformationMode,
                         float tickDelta,
                         MultiBufferSource vertexConsumers,
                         @Nullable LivingEntity entity,
                         int light,
                         int overlay) {

        public RenderContext(@Nullable String modelType,
                             PoseStack matrices,
                             ItemStack stack,
                             ItemDisplayContext transformationMode,
                             float tickDelta,
                             MultiBufferSource vertexConsumers,
                             @Nullable LivingEntity entity,
                             int light,
                             int overlay) {
            this(modelType, matrices, matrices.last().pose(), stack, transformationMode, tickDelta, vertexConsumers, entity, light, overlay);
        }

        @Nullable
        public LivingEntity entity() {
            return entity;
        }

        public LivingEntity getEntitySave() {
            return entity == null ? Minecraft.getInstance().player : entity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RenderContext other)) return false;

            return Objects.equals(modelType, other.modelType)
                   && stack == other.stack
                   && transformationMode == other.transformationMode
                   && entity == other.entity;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(modelType);
            result = 31 * result + System.identityHashCode(stack);
            result = 31 * result + transformationMode.hashCode();
            result = 31 * result + System.identityHashCode(entity);
            return result;
        }


    }
}
