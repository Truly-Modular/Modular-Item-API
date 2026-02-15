package smartin.miapi.client.model.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialIcons;

@Environment(EnvType.CLIENT)
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
    public void render(RenderContext context) {
        int light = context.light();
        if (doTick) {
            lastTick += context.tickDelta();
            if (lastTick > 1) {
                //i dont like this tick code, its bad but functional
                toRenderEntity.tick();
                lastTick -= 1;
            }
        }
        context.matrices().pushPose();
        transform.applyPosition(context.matrices());
        if (spinSettings != null) {
            spinSettings.multiplyMatrices(context.matrices());
        }
        if (fullBright) {
            light = LightTexture.FULL_BRIGHT;
        }
        Minecraft.getInstance().getEntityRenderDispatcher().render(
                toRenderEntity, 0, 0, 0, 0,
                context.tickDelta(),
                context.matrices(),
                context.vertexConsumers(),
                light);
        context.matrices().popPose();
    }
}
