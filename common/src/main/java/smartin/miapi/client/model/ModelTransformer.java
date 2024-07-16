package smartin.miapi.client.model;

import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

public class ModelTransformer {
    /*
0: X position
1: Y position
2: Z position
3: Color (assumed to be stored as a packed integer)
4: Texture U coordinate
5: Texture V coordinate
6: Lighting value (assumed to be stored as a packed integer)
7: Normals (assumed to be stored as a packed integer)
*/
    private static final Map<BakedModel, List<BakedQuad>> inverseMap = new WeakHashMap<>();
    private static final Map<BakedModel, List<BakedQuad>> reScaledMap = new WeakHashMap<>();
    private static final Map<BakedModel, List<BakedQuad>> inversedRescaledMap = new WeakHashMap<>();

    public static void clearCaches() {
        inverseMap.clear();
        reScaledMap.clear();
        inversedRescaledMap.clear();
    }


    public static List<BakedQuad> getInverse(BakedModel bakedModel, RandomSource random) {
        return inverseMap.computeIfAbsent(bakedModel, model -> {
            List<BakedQuad> rawQuads = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                rawQuads.addAll(model.getQuads(null, direction, random));
            }
            List<BakedQuad> redoneQuads = new ArrayList<>();
            rawQuads.forEach(bakedQuad -> {
                redoneQuads.add(new BakedQuad(inverse(bakedQuad.getVertices()), bakedQuad.getTintIndex(), bakedQuad.getDirection(), bakedQuad.getSprite(), bakedQuad.isShade()));
            });
            return redoneQuads;
        });
    }

    public static List<BakedQuad> getRescale(BakedModel bakedModel, RandomSource random) {
        return reScaledMap.computeIfAbsent(bakedModel, model -> {
            List<BakedQuad> rawQuads = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                rawQuads.addAll(model.getQuads(null, direction, random));
            }
            List<BakedQuad> redoneQuads = new ArrayList<>();
            rawQuads.forEach(bakedQuad -> {
                float uStart = bakedQuad.getSprite().getU0();
                float uScale = 1 / (bakedQuad.getSprite().getU1() - bakedQuad.getSprite().getU0());
                float vStart = bakedQuad.getSprite().getV0();
                float vScale = 1 / (bakedQuad.getSprite().getV1() - bakedQuad.getSprite().getV0());
                redoneQuads.add(new BakedQuad(rescale(bakedQuad.getVertices(), uStart, uScale, vStart, vScale), bakedQuad.getTintIndex(), bakedQuad.getDirection(), bakedQuad.getSprite(), bakedQuad.isShade()));
            });
            return redoneQuads;
        });
    }

    public static List<BakedQuad> getRescaleInverse(BakedModel bakedModel, RandomSource random) {
        return inversedRescaledMap.computeIfAbsent(bakedModel, model -> {
            List<BakedQuad> rawQuads = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                rawQuads.addAll(model.getQuads(null, direction, random));
            }
            List<BakedQuad> redoneQuads = new ArrayList<>();
            rawQuads.forEach(bakedQuad -> {
                float uStart = bakedQuad.getSprite().getU0();
                float uScale = 1 / (bakedQuad.getSprite().getU1() - bakedQuad.getSprite().getU0());
                float vStart = bakedQuad.getSprite().getV0();
                float vScale = 1 / (bakedQuad.getSprite().getV1() - bakedQuad.getSprite().getV0());
                redoneQuads.add(new BakedQuad(inverse(rescale(bakedQuad.getVertices(), uStart, uScale, vStart, vScale)), bakedQuad.getTintIndex(), bakedQuad.getDirection(), bakedQuad.getSprite(), bakedQuad.isShade()));
            });
            return redoneQuads;
        });
    }

    private static int[] rescale(int[] raw, float uStart, float uScale, float vStart, float vScale) {
        int[] copiedArray = new int[raw.length];
        System.arraycopy(raw, 0, copiedArray, 0, raw.length);
        for (int i = 0; i < raw.length / 8; i++) {
            copiedArray[i * 8 + 4] = Float.floatToRawIntBits((Float.intBitsToFloat(raw[i * 8 + 4]) - uStart) * uScale);
            copiedArray[i * 8 + 5] = Float.floatToRawIntBits((Float.intBitsToFloat(raw[i * 8 + 5]) - vStart) * vScale);
        }
        return copiedArray;
    }

    private static int[] inverse(int[] raw) {
        int[] copiedArray = new int[raw.length];
        System.arraycopy(raw, 0, copiedArray, 0, raw.length);
        for (int j = 0; j < 8; j++) {
            copiedArray[j] = raw[2 * 8 + j];
            copiedArray[8 + j] = raw[8 + j];
            copiedArray[2 * 8 + j] = raw[j];
            copiedArray[3 * 8 + j] = raw[3 * 8 + j];
        }
        return copiedArray;
    }
}
