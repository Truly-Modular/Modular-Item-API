package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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
    public void render(PoseStack matrixStack, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        if (doTick) {
            if (lastTick > tickDelta) {
                //i dont like this tick code, its bad but functional
                toRenderEntity.tick();
            }
            lastTick = tickDelta;
        }
        matrixStack.pushPose();
        transform.applyPosition(matrixStack);
        if (spinSettings != null) {
            spinSettings.multiplyMatrices(matrixStack);
        }
        if (fullBright) {
            light = LightTexture.FULL_BRIGHT;
        }
        Minecraft.getInstance().getEntityRenderDispatcher().render(
                toRenderEntity, 0, 0, 0, 0,
                tickDelta,
                matrixStack,
                vertexConsumers,
                light);
        matrixStack.popPose();
    }
}
