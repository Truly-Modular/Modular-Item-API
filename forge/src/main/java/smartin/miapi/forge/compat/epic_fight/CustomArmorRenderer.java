package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.client.atlas.ArmorModelManager;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

@OnlyIn(Dist.CLIENT)
public class CustomArmorRenderer extends ModelRenderLayer<
        AbstractClientPlayerEntity,
        AbstractClientPlayerPatch<AbstractClientPlayerEntity>,
        PlayerEntityModel<AbstractClientPlayerEntity>,
        CustomArmorRenderer.ModularItemFeatureLayer,
        HumanoidMesh> {
    public CustomArmorRenderer(MeshProvider mesh) {
        super(mesh);
    }

    @Override
    protected void renderLayer(
            AbstractClientPlayerPatch<AbstractClientPlayerEntity> abstractClientPlayerEntityAbstractClientPlayerPatch,
            AbstractClientPlayerEntity abstractClientPlayerEntity,
            @Nullable ModularItemFeatureLayer modularItemFeatureLayer,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int i, OpenMatrix4f[] openMatrix4fs, float v, float v1, float v2, float v3) {
        Miapi.LOGGER.info("render layer " + EquipmentSlot.CHEST);
        ArmorModelManager.renderArmorPiece(
                matrixStack,
                vertexConsumerProvider,
                i,
                EquipmentSlot.CHEST,
                abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.MAINHAND),
                abstractClientPlayerEntity,
                modularItemFeatureLayer.model.getModel(),
                modularItemFeatureLayer.model.getModel());
    }

    public class ModularItemFeatureLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
        public FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> model;

        public ModularItemFeatureLayer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
            super(context);
            this.model = context;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
            Miapi.LOGGER.info("render feature layer ");
        }
    }
}
