package smartin.miapi.client.renderer;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;

public class RescaledVertexConsumer implements VertexConsumer {
    public VertexConsumer delegate;
    float uStart;
    float uScale;
    float vStart;
    float vScale;

    public RescaledVertexConsumer(VertexConsumer delegate, Sprite sprite) {
        this.delegate = delegate;
        setSprite(sprite);
    }

    public void setSprite(Sprite sprite) {
        uStart = sprite.getMinU();
        uScale = 1 / (sprite.getMaxU() - sprite.getMinU());
        vStart = sprite.getMinV();
        vScale = 1 / (sprite.getMaxV() - sprite.getMinV());
    }

    @Override
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        u = ((u - uStart) * uScale);
        v = ((v - vStart) * vScale);
        this.vertex(x, y, z);
        this.color(red, green, blue, alpha);
        this.texture(u, v);
        this.overlay(overlay);
        this.light(light);
        this.normal(normalX, normalY, normalZ);
        this.next();
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer overlay(int v) {
        return delegate.overlay(v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
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
