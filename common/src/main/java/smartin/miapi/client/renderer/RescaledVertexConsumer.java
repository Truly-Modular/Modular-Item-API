package smartin.miapi.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class RescaledVertexConsumer implements VertexConsumer {
    public VertexConsumer delegate;
    float uStart;
    float uScale;
    float vStart;
    float vScale;

    public RescaledVertexConsumer(VertexConsumer delegate, TextureAtlasSprite sprite) {
        this.delegate = delegate;
        setSprite(sprite);
    }

    public void setSprite(TextureAtlasSprite sprite) {
        uStart = sprite.getU0();
        uScale = 1 / (sprite.getU1() - sprite.getU0());
        vStart = sprite.getV0();
        vScale = 1 / (sprite.getV1() - sprite.getV0());
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
        u = ((u - uStart) * uScale);
        v = ((v - vStart) * vScale);
        this.addVertex(x, y, z);
        this.setColor(color);
        this.setUv(u, v);
        this.setOverlay(packedOverlay);
        this.setLight(packedLight);
        this.setNormal(normalX, normalY, normalZ);
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
