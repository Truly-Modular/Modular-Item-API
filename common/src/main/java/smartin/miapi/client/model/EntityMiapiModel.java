package smartin.miapi.client.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialIcons;

public class EntityMiapiModel implements MiapiModel {
    Entity toRenderEntity;
    float lastTick = 0;
    Transform transform;
    public boolean fullBright = true;
    public boolean doTick = true;
    public MaterialIcons.SpinSettings spinSettings = null;

    public EntityMiapiModel(Entity entity, Transform transform) {
        toRenderEntity = entity;
        this.transform = transform;
    }

    @Override
    public void render(MatrixStack matrixStack, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        if (doTick) {
            if (lastTick > tickDelta) {
                //i dont like this tick code, its bad but functional
                toRenderEntity.tick();
            }
            lastTick = tickDelta;
        }
        matrixStack.push();
        transform.applyPosition(matrixStack);
        if (spinSettings != null) {
            spinSettings.multiplyMatrices(matrixStack);
        }
        if (fullBright) {
            light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(
                toRenderEntity, 0, 0, 0, 0,
                tickDelta,
                matrixStack,
                vertexConsumers,
                light);
        matrixStack.pop();
    }
}
