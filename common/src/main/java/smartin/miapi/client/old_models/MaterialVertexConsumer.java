package smartin.miapi.client.old_models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.modules.material.Material;

/**
 * replaced with new logic - this prob should be removed like all classes in this package
 */
@Environment(EnvType.CLIENT)
public class MaterialVertexConsumer implements VertexConsumer {
    public VertexConsumer delegate;
    protected final int x;
    protected final int y;

    public MaterialVertexConsumer(VertexConsumer delegate, Material material) {
        this.delegate = delegate;
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getPalette().getSpriteId());
        if(sprite == null){
            sprite = MiapiClient.materialAtlasManager.getMaterialSprite(MaterialAtlasManager.BASE_MATERIAL_ID);
        }
        if (sprite != null) {
            //Miapi.LOGGER.error(material.getKey() + " " + sprite.getY());
            this.x = sprite.getX();
            this.y = sprite.getY();
        } else {
            this.x = 0;
            this.y = 0;
        }
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
    public VertexConsumer overlay(int v) {
        return delegate.overlay(x, y);
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
