package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.mixin.client.ElytraEntityModelAccessor;
import smartin.miapi.mixin.client.ElytraFeatureRendererAccessor;

public class ModularElytraFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer {
    private static final ResourceLocation SKIN = ResourceLocation.parse("textures/entity/elytra.png");
    private final ElytraModel<T> elytra;

    public ModularElytraFeatureRenderer(RenderLayerParent<T, M> context, EntityModelSet loader) {
        super(context, loader);
        elytra = ((ElytraFeatureRendererAccessor) this).getElytra();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l) {
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (itemStack.getItem() instanceof VisualModularItem) {
            matrixStack.pushPose();
            matrixStack.translate(0.0F, 0.0F, 0.125F);
            this.getParentModel().copyPropertiesTo(this.elytra);
            this.elytra.setupAnim((T) livingEntity, f, g, j, k, l);
            MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
            if (miapiItemModel != null) {
                matrixStack.pushPose();
                ModelPart leftWing = ((ElytraEntityModelAccessor) elytra).getLeftWing();
                leftWing.translateAndRotate(matrixStack);
                miapiItemModel.render("left_wing", matrixStack, ItemDisplayContext.HEAD, 0, vertexConsumerProvider, i, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();

                matrixStack.pushPose();
                ModelPart rightWing = ((ElytraEntityModelAccessor) elytra).getRightWing();
                rightWing.translateAndRotate(matrixStack);
                miapiItemModel.render("right_wing", matrixStack, ItemDisplayContext.HEAD, 0, vertexConsumerProvider, i, OverlayTexture.NO_OVERLAY);
                matrixStack.popPose();
            }
            matrixStack.popPose();
        }
    }
}