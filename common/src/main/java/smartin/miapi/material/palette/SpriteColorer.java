package smartin.miapi.material.palette;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * This class implements {@link MaterialRenderController} by recoloring the base Sprite
 * it works hand-in-hand with {@link MaterialSpriteManager} to accomplish that
 */
@Environment(EnvType.CLIENT)
public abstract class SpriteColorer implements MaterialRenderController {
    protected static Map<TextureAtlasSprite, VertexConsumer> lookupMap = new WeakHashMap<>();
    public Material material;

    public SpriteColorer(Material material) {
        this.material = material;
    }

    public MaterialRecoloredSpriteHolder createSpriteManager(SpriteContents spriteContents) {
        return new MaterialRecoloredSpriteHolder(spriteContents);
    }


    /**
     * This function is called if {@link SpriteColorer#doTick()} is true. This allows SpriteColorers to NOT update the uploaded image if they dont want to
     *
     * @param nativeImageConsumer call this to upload a new NativeImage
     * @param spriteContents      the contents of the original sprite in question
     */
    public void tick(Consumer<NativeImage> nativeImageConsumer, SpriteContents spriteContents) {
        nativeImageConsumer.accept(transform(spriteContents));
    }

    /**
     * This method transform a module Sprite to be recolored with this Material Colorer
     *
     * @param originalSprite the original Sprite
     * @return the transformed recolored version
     */
    public abstract NativeImage transform(SpriteContents originalSprite);

    /**
     * Animated Materials will recolor every frame, use this carefully
     *
     * @return if the Material is animated and needs to call {@link SpriteColorer#transform(SpriteContents)} every frame
     */
    public abstract boolean doTick();

    @Environment(EnvType.CLIENT)
    public VertexConsumer getVertexConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite originalSprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {
        return MaterialSpriteManager.getVertexConsumer(vertexConsumers, originalSprite, material, this);
    }

    public boolean isAnimatedSprite(SpriteContents spriteContents) {
        return SpriteColorer.isAnimatedSpriteStatic(spriteContents);
    }

    public static boolean isAnimatedSpriteStatic(SpriteContents spriteContents) {
        try (SpriteTicker animator = spriteContents.createTicker()) {
            if (animator != null) {
                return true;
            }
        }
        return false;
    }

    public class MaterialRecoloredSpriteHolder {
        SpriteContents lastRecolouredSprite;
        boolean isAnimated;

        public MaterialRecoloredSpriteHolder(SpriteContents modelSprite) {
            lastRecolouredSprite = modelSprite;
            isAnimated = isAnimatedSprite(modelSprite);
        }

        public boolean requireTick() {
            return isAnimated || SpriteColorer.this.doTick();
        }

        public Material getMaterial() {
            return SpriteColorer.this.material;
        }

        public NativeImage recolor() {
            return SpriteColorer.this.transform(lastRecolouredSprite);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MaterialRecoloredSpriteHolder other) {
                return other.lastRecolouredSprite.equals(lastRecolouredSprite) && material.equals(other.getMaterial());
            }
            return super.equals(obj);
        }
    }
}
