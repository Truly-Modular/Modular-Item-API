package smartin.miapi.modules.properties.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.ItemProjectileRenderer;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

public class InAirProjectileTransform extends CodecProperty<Transform> {
    public static final ResourceLocation KEY = Miapi.id("projectile_transform");
    public static InAirProjectileTransform property;

    public InAirProjectileTransform() {
        super(Transform.CODEC);
        property = this;
        if (Platform.getEnv() == EnvType.CLIENT) {
            clientSetup();
        }
    }

    @Environment(EnvType.CLIENT)
    public void clientSetup() {
        MiapiProjectileEvents.ClientEvents.MODULAR_PROJECTILE_RENDER_EVENT.register(new MiapiProjectileEvents.ModularProjectileRenderEvent() {
            @Override
            public EventResult hit(ItemProjectileRenderer renderer, ItemStack stack, ItemProjectileEntity entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
                getData(stack).ifPresent(transform -> {
                    transform.applyPosition(matrixStack);
                });
                return EventResult.pass();
            }
        });
    }

    @Override
    public Transform merge(Transform left, Transform right, MergeType mergeType) {
        return Transform.merge(left, right);
    }
}
