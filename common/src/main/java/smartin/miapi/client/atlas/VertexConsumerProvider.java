package smartin.miapi.client.atlas;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.Function;

/**
 * this is an optimisation class.
 * This only works on the same TextureAtlasSpriteTarget.
 * it exists to mainly cache lookups from {@link MaterialSpriteManager} to the class that does the actual rendering.
 * and to flatten all the required lookups.
 * This class should be instantiated and cached by the actual rendering class.
 */
public class VertexConsumerProvider {
    public int counter = 0;
    public boolean isFast = false;
    public MaterialSpriteManager.Holder spriteHolder;
    public MaterialSpriteManager.SpriteSlot spriteSlot;
    public Function<MultiBufferSource, VertexConsumer> vanillaVCGetter;
    public Function<MultiBufferSource, VertexConsumer> getRenderSaveVC;

    public void clean() {
        spriteHolder = null;
        spriteSlot = null;
    }
}
