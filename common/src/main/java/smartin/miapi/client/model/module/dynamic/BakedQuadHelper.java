package smartin.miapi.client.model.module.dynamic;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public class BakedQuadHelper {

    private static final int FULL_BRIGHT =
            LightTexture.pack(15, 15);


    private static int packColor(float r, float g, float b, float a) {
        int ai = (int)(a * 255.0f) & 0xFF;
        int ri = (int)(r * 255.0f) & 0xFF;
        int gi = (int)(g * 255.0f) & 0xFF;
        int bi = (int)(b * 255.0f) & 0xFF;
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    private static int packNormal(float x, float y, float z) {
        int nx = (int)(x * 127.0f) & 0xFF;
        int ny = (int)(y * 127.0f) & 0xFF;
        int nz = (int)(z * 127.0f) & 0xFF;
        return nx | (ny << 8) | (nz << 16);
    }

    public static BakedQuad buildQuad(
            Vector3f p0a,
            Vector3f p0b,
            Vector3f p1a,
            Vector3f p1b,
            float r, float g, float b, float a,
            TextureAtlasSprite sprite
    ) {
        int[] data = new int[32];

        int color = packColor(r, g, b, a);
        int light = FULL_BRIGHT;
        int normal = packNormal(0, 1, 0);

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        putVertex(data, 0,  p0a, color, u0, v0, light, normal);
        putVertex(data, 8,  p0b, color, u1, v0, light, normal);
        putVertex(data, 16, p1b, color, u1, v1, light, normal);
        putVertex(data, 24, p1a, color, u0, v1, light, normal);

        return new BakedQuad(
                data,
                -1,
                Direction.UP,
                sprite,
                false
        );
    }

    private static void putVertex(
            int[] data,
            int offset,
            Vector3f pos,
            int color,
            float u, float v,
            int light,
            int normal
    ) {
        data[offset]     = Float.floatToRawIntBits(pos.x());
        data[offset + 1] = Float.floatToRawIntBits(pos.y());
        data[offset + 2] = Float.floatToRawIntBits(pos.z());
        data[offset + 3] = color;
        data[offset + 4] = Float.floatToRawIntBits(u);
        data[offset + 5] = Float.floatToRawIntBits(v);
        data[offset + 6] = light;
        data[offset + 7] = normal;
    }


}
