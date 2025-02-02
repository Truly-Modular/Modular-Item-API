package smartin.miapi.material.palette;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.material.base.Material;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * This class implements {@link MaterialRenderController} by recoloring the base Sprite
 * it works hand-in-hand with {@link MaterialSpriteManager} to accomplish that
 */
public abstract class SpriteColorer implements MaterialRenderController {
    public static Map<TextureAtlasSprite, RescaledVertexConsumer> lookupMap = new WeakHashMap<>();
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
        ResourceLocation replaceId = MaterialSpriteManager.getMaterialSprite(originalSprite, material, this);
        RenderType atlasRenderLayer = RenderType.entityTranslucentCull(replaceId);
        VertexConsumer atlasConsumer = ItemRenderer.getFoilBufferDirect(vertexConsumers, atlasRenderLayer, true, false);
        return getVertexConsumer(atlasConsumer, originalSprite);
    }

    public static RescaledVertexConsumer getVertexConsumer(VertexConsumer vertexConsumer, TextureAtlasSprite sprite) {
        RescaledVertexConsumer rescaled = lookupMap.computeIfAbsent(sprite, (s) -> new RescaledVertexConsumer(vertexConsumer, sprite));
        rescaled.delegate = vertexConsumer;
        return rescaled;
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
