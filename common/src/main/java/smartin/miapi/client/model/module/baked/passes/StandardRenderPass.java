package smartin.miapi.client.model.module.baked.passes;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.model.module.baked.DoubleQuadCache;
import smartin.miapi.modules.ModuleInstance;

public class StandardRenderPass implements RenderPass {
    DoubleQuadCache[] quadCaches;

    public StandardRenderPass(ModuleInstance moduleInstance, ItemStack stack, ItemDisplayContext context, ModelHolder modelHolder, BakedModelCache cache, RandomSource random) {
        quadCaches = cache.getForward(modelHolder.colorProvider(), stack, moduleInstance, context, random);
    }

    @Override
    public void render(MiapiModel.RenderContext context, float[] colors, float alpha, int light) {
        try {
            context.matrices().pushPose();
            for (DoubleQuadCache batch : quadCaches) {
                batch.render(context.vertexConsumers(), context.matrices().last(), colors[0], colors[1], colors[2], alpha, light, context.overlay());
            }
        } catch (RuntimeException renderError) {
            Miapi.LOGGER.info("Error while rendering:" , renderError);
        } finally {
            context.matrices().popPose();
        }
    }
}
