package smartin.miapi.client.model.module.baked;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.client.model.ModelTransformer;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BakedModelCache {
    BakedModel model;
    DoubleQuadCache[] forward;
    DoubleQuadCache[] backwards;
    BakedQuad[] trim;


    public BakedModelCache(BakedModel model) {
        this.model = model;
    }

    public static DoubleQuadCache[] buildBatches(BakedModel model, RandomSource random,
                                                 ColorProvider colorProvider,
                                                 ItemStack itemStack,
                                                 ModuleInstance moduleInstance,
                                                 ItemDisplayContext context) {
        Map<TextureAtlasSprite, List<BakedQuad>> temp = new HashMap<>();

        for (Direction dir : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(null, dir, random);

            for (BakedQuad quad : quads) {
                temp.computeIfAbsent(quad.getSprite(), s -> new ArrayList<>())
                        .add(quad);
            }
        }

        DoubleQuadCache[] result = new DoubleQuadCache[temp.size()];
        int i = 0;
        for (Map.Entry<TextureAtlasSprite, List<BakedQuad>> entry : temp.entrySet()) {
            VertexConsumerProvider provider = new VertexConsumerProvider();
            colorProvider.getConsumer(entry.getKey(), itemStack, moduleInstance, context, provider);
            List<BakedQuad> quads = entry.getValue();
            result[i] = new DoubleQuadCache(quads.toArray(new BakedQuad[quads.size()]), provider, entry.getKey());
            i++;
        }
        return result;
    }

    public DoubleQuadCache[] getForward(ColorProvider colorProvider, ItemStack itemStack, ModuleInstance moduleInstance, ItemDisplayContext context, RandomSource random) {
        if (forward == null) {
            forward = buildBatches(model, random, colorProvider, itemStack, moduleInstance, context);
        }
        return forward;
    }

    public DoubleQuadCache[] getBackwards(ColorProvider colorProvider, ItemStack itemStack, ModuleInstance moduleInstance, ItemDisplayContext context, RandomSource random) {
        if (backwards == null) {
            DoubleQuadCache[] forwardArray = getForward(colorProvider, itemStack, moduleInstance, context, random);
            backwards = new DoubleQuadCache[forwardArray.length];
            for (int i = 0; i < forwardArray.length; i++) {
                VertexConsumerProvider provider = new VertexConsumerProvider();
                DoubleQuadCache cache = forward[i];
                colorProvider.getConsumer(cache.sprite, itemStack, moduleInstance, context, provider);
                backwards[i] = new DoubleQuadCache(ModelTransformer.getInverse(cache.original), provider, cache.sprite);
            }
        }
        return backwards;
    }

    public BakedQuad[] getTrim(RandomSource random) {
        if (trim == null) {
            trim = ModelTransformer.getRescale(model, random).toArray(new BakedQuad[0]);
        }
        return trim;
    }
}
