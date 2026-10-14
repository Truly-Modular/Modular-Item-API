package smartin.miapi.client.model.module.baked;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelHelper {
    public static BakedModel resolve(BakedModel model, ItemStack stack, @Nullable LivingEntity entity, int light) {
        BakedModel override = model.getOverrides().resolve(model, stack, Minecraft.getInstance().level, entity, light);
        if (model != null) {
            model = override;
        }
        return model;
    }

    public static List<Pair<TextureAtlasSprite, List<BakedQuad>>> buildBatches(BakedModel model, RandomSource random) {
        Map<TextureAtlasSprite, List<BakedQuad>> temp = new HashMap<>();
        for (Direction dir : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(null, dir, random);
            for (BakedQuad quad : quads) {
                temp.computeIfAbsent(quad.getSprite(), s -> new ArrayList<>())
                        .add(quad);
            }
        }
        List<Pair<TextureAtlasSprite, List<BakedQuad>>> result = new ArrayList<>(temp.size());
        for (Map.Entry<TextureAtlasSprite, List<BakedQuad>> entry : temp.entrySet()) {
            result.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
