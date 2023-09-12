package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import smartin.miapi.modules.properties.material.Material;

@Environment(EnvType.CLIENT)
public class MaterialVertexConsumer implements VertexConsumer {
    protected final VertexConsumer delegate;
    protected final int x;
    protected final int y;

    public MaterialVertexConsumer(VertexConsumer delegate, Material material) {
        this.delegate = delegate;
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getSpriteId());
        this.x = sprite.getX();
        this.y = sprite.getY();
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
        return delegate.overlay(x, y);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return delegate.light(u,v);
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
