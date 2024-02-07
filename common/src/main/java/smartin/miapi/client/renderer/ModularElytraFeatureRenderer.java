package smartin.miapi.client.renderer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.mixin.client.ElytraEntityModelAccessor;
import smartin.miapi.mixin.client.ElytraFeatureRendererAccessor;

public class ModularElytraFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraFeatureRenderer {
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");
    private final ElytraEntityModel<T> elytra;

    public ModularElytraFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        super(context, loader);
        elytra = ((ElytraFeatureRendererAccessor) this).getElytra();
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l) {
        ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
        if (itemStack.getItem() instanceof ModularItem) {
            matrixStack.push();
            matrixStack.translate(0.0F, 0.0F, 0.125F);
            this.getContextModel().copyStateTo(this.elytra);
            this.elytra.setAngles((T) livingEntity, f, g, j, k, l);
            MiapiItemModel miapiItemModel = MiapiItemModel.getItemModel(itemStack);
            if (miapiItemModel != null) {
                matrixStack.push();
                ModelPart leftWing = ((ElytraEntityModelAccessor) elytra).getLeftWing();
                leftWing.rotate(matrixStack);
                miapiItemModel.render("left_wing", matrixStack, ModelTransformationMode.HEAD, 0, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
                matrixStack.pop();

                matrixStack.push();
                ModelPart rightWing = ((ElytraEntityModelAccessor) elytra).getRightWing();
                rightWing.rotate(matrixStack);
                miapiItemModel.render("right_wing", matrixStack, ModelTransformationMode.HEAD, 0, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
                matrixStack.pop();
            }
            matrixStack.pop();
        }
    }
}