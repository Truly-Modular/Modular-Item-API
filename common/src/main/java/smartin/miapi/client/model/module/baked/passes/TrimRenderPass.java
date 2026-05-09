package smartin.miapi.client.model.module.baked.passes;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.renderer.TrimRenderer;

public class TrimRenderPass implements RenderPass{
    ModelHolder modelHolder;
    BakedQuad[] bakedQuads;

    public TrimRenderPass(ModelHolder modelHolder, BakedModelCache cache, RandomSource random){
        this.modelHolder = modelHolder;
        this.bakedQuads = cache.getTrim(random);
    }

    @Override
    public void render(MiapiModel.RenderContext context, float[] colors, float alpha, int light) {
        Holder<ArmorMaterial> armorMaterial = (context.stack().getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : null;

        if (armorMaterial != null && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            for(BakedQuad quad: bakedQuads){
                TrimRenderer.renderTrims(context.matrices(), quad, modelHolder.trimMode(), light, context.vertexConsumers(), armorMaterial, context.stack());
            }
        }
    }
}
