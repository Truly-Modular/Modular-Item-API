package smartin.miapi.modules.material.palette;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Animator;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class implements {@link MaterialRenderController} by recoloring the base Sprite
 * it works hand-in-hand with {@link MaterialSpriteManager} to accomplish that
 */
public abstract class SpriteColorer implements MaterialRenderController {
    public static Map<Sprite, RescaledVertexConsumer> lookupMap = new WeakHashMap<>();
    public Material material;

    public SpriteColorer(Material material) {
        this.material = material;
    }

    public MaterialRecoloredSpriteHolder createSpriteManager(SpriteContents spriteContents) {
        return new MaterialRecoloredSpriteHolder(spriteContents);
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
    public abstract boolean isAnimated();

    @Environment(EnvType.CLIENT)
    public VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, Sprite originalSprite, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
        Identifier replaceId = MaterialSpriteManager.getMaterialSprite(originalSprite, material, this);
        RenderLayer atlasRenderLayer = RenderLayer.getEntityTranslucentCull(replaceId);
        VertexConsumer atlasConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, atlasRenderLayer, true, false);
        return get(atlasConsumer, originalSprite);
    }

    public RescaledVertexConsumer get(VertexConsumer vertexConsumer, Sprite sprite) {
        RescaledVertexConsumer rescaled = lookupMap.computeIfAbsent(sprite, (s) -> new RescaledVertexConsumer(vertexConsumer, sprite));
        rescaled.delegate = vertexConsumer;
        return rescaled;
    }

    public boolean isAnimatedSprite(SpriteContents spriteContents) {
        return SpriteColorer.isAnimatedSpriteStatic(spriteContents);
    }
    public static boolean isAnimatedSpriteStatic(SpriteContents spriteContents) {
        try (Animator animator = spriteContents.createAnimator()) {
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
            return isAnimated || SpriteColorer.this.isAnimated();
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
