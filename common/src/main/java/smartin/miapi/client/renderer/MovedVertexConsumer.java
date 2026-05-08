package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class MovedVertexConsumer implements VertexConsumer {
    public VertexConsumer delegate;
    public float uOffset;
    public float vOffset;

    public MovedVertexConsumer(VertexConsumer delegate, TextureAtlasSprite original, TextureAtlasSprite target) {
        this.delegate = delegate;
        uOffset = target.getU0() - original.getU0();
        vOffset = target.getV0() - original.getV0();
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
        u = u + uOffset;
        v = v + vOffset;
        //Minecraft.getInstance().getProfiler().push("Actual Vertex");
        this.addVertex(x, y, z);
        this.setColor(color);
        this.setUv(u, v);
        this.setOverlay(packedOverlay);
        this.setLight(packedLight);
        this.setNormal(normalX, normalY, normalZ);
        //Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return delegate.addVertex(x, y, z);
    }


    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return delegate.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setOverlay(int v) {
        return delegate.setOverlay(v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return delegate.setNormal(x, y, z);
    }
}
