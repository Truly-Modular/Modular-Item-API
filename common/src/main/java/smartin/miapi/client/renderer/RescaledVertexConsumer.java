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
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        u = ((u - uStart) * uScale);
        v = ((v - vStart) * vScale);
        this.addVertex(x, y, z);
        this.setColor(red, green, blue, alpha);
        this.setUv(u, v);
        this.setOverlay(overlay);
        this.setLight(light);
        this.setNormal(normalX, normalY, normalZ);
        this.next();
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
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

    @Override
    public void next() {
        delegate.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        delegate.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        delegate.unfixColor();
    }
}
