package smartin.miapi.client.model.module.baked.passes;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.model.module.baked.DoubleQuadCache;
import smartin.miapi.client.renderer.ObjectUVVertexConsumer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;

public class VanillaGlintRenderPass implements RenderPass {
    DoubleQuadCache[] quadCaches;
    GlintProperty.GlintSettings settings;
    float alphaAdjust;

    public VanillaGlintRenderPass(ModuleInstance moduleInstance, ItemStack stack, GlintProperty.GlintSettings settings, ItemDisplayContext context, ModelHolder modelHolder, BakedModelCache cache, RandomSource random) {
        quadCaches = cache.getForward(modelHolder.colorProvider(), stack, moduleInstance, context, random);
        if (
                context == ItemDisplayContext.HEAD
        ) {
            alphaAdjust = MiapiConfig.getClientConfig().enchantingGlint.armorEnchantmentAlphaAdjust;
        } else {
            alphaAdjust = 1.0f;
        }
        this.settings = settings;
    }

    @Override
    public void render(MiapiModel.RenderContext context, float[] colors, float alpha, int light) {
        if (context.glint()) {
            VertexConsumer glintConsumer = new ObjectUVVertexConsumer(
                    context.vertexConsumers().getBuffer(RenderType.entityGlintDirect()),
                    context.objectSpace(), false, 1.0f
            );
            for (DoubleQuadCache batch : quadCaches) {
                for (BakedQuad quad : batch.original) {
                    Color glintColor = settings.getColor();
                    glintConsumer.putBulkData(context.matrices().last(), quad,
                            glintColor.redAsFloat(),
                            glintColor.greenAsFloat(),
                            glintColor.blueAsFloat(),
                            glintColor.alphaAsFloat() * alpha * alphaAdjust, light, context.overlay());
                }
            }
        }
    }
}
