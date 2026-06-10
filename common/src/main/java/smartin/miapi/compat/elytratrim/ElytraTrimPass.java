package smartin.miapi.compat.elytratrim;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import dev.kikugie.elytratrims.client.render.ETRenderer;
import dev.kikugie.elytratrims.client.resource.ETAtlasHolder;
import dev.kikugie.elytratrims.common.access.FeatureAccess;
import dev.kikugie.elytratrims.common.compat.ShowMeYourSkinCompat;
import dev.kikugie.elytratrims.common.util.ColorKt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.model.module.baked.DoubleQuadCache;
import smartin.miapi.client.model.module.baked.passes.RenderPass;
import smartin.miapi.modules.ModuleInstance;

import static dev.kikugie.elytratrims.client.render.ETRenderer.shouldRender;
import static smartin.miapi.compat.elytratrim.ElytraTrimsCompat.getTrimSprite;

public class ElytraTrimPass implements RenderPass {
    private final DoubleQuadCache[] quads;
    private final BakedQuad[] justQuads;
    private final ArmorTrim trim;
    private final ItemStack stack;

    public ElytraTrimPass(ArmorTrim trim,
                          ModelHolder holder,
                          ModuleInstance moduleInstance,
                          ItemStack stack,
                          ItemDisplayContext context,
                          BakedModelCache cache,
                          RandomSource random) {
        this.quads = cache.getForward(holder.colorProvider(), stack, moduleInstance, context, random);
        this.justQuads = cache.getTrim(random);
        this.trim = trim;
        this.stack = stack;
    }

    @Override
    public void render(MiapiModel.RenderContext context,

                       float[] color,
                       float oldAlpha,
                       int light) {

        TextureAtlasSprite sprite = getTrimSprite(trim);
        if (sprite == null) {
            return;
        }
        int effectiveLight = FeatureAccess.INSTANCE.hasGlow(stack) && (context.entity() == null || shouldRender(dev.kikugie.elytratrims.client.config.RenderType.GLOW, context.entity())) ? 16711935 : light;
        int colorInt = new Color(color[0], color[1], color[2], oldAlpha).argb();
        float alpha = ShowMeYourSkinCompat.INSTANCE.getElytraTransparency(ColorKt.scaled(ColorKt.alpha(colorInt)), context.entity());
        int newColor = ColorKt.withAlpha(colorInt, (int) (alpha * (float) 255) & 255);
        Color color1 = new Color(newColor);

        VertexConsumer consumer =
                sprite.wrap(
                        context.vertexConsumers()
                                .getBuffer(ETRenderer.layer.invoke(ETAtlasHolder.INSTANCE.getId()))
                );
        for (BakedQuad quad : justQuads) {
            consumer.putBulkData(
                    context.matrices().last(),
                    quad,
                    color1.redAsFloat(),
                    color1.greenAsFloat(),
                    color1.blueAsFloat(),
                    alpha,
                    effectiveLight,
                    context.overlay());
        }
        for (DoubleQuadCache c : quads) {
            for (BakedQuad quad : c.original) {
                consumer.putBulkData(
                        context.matrices().last(),
                        quad,
                        color[0],
                        color[1],
                        color[2],
                        oldAlpha,
                        light,
                        context.overlay());
            }
        }
    }
}