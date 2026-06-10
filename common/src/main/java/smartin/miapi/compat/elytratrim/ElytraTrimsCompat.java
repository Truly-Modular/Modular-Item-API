package smartin.miapi.compat.elytratrim;

import dev.kikugie.elytratrims.client.resource.ETAtlasHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.model.module.baked.passes.RenderPass;
import smartin.miapi.events.ClientEvents;
import smartin.miapi.item.modular.items.armor.ModularElytraItem;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.SpriteColorer;
import smartin.miapi.material.palette.SpriteOverlayer;
import smartin.miapi.mixin.client.TextureAtlasAccessor;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

public final class ElytraTrimsCompat {
    public static void init() {
        ClientEvents.RESOLVE_COLOR_PROVIDER.register(
                ElytraTrimsCompat::resolveColorProvider
        );
        //RenderPass.COLLECT_PASSES.register(ElytraTrimsCompat::findPasses);
    }

    private static void findPasses(List<RenderPass> passes,
                                   ModelHolder holder,
                                   ModuleInstance moduleInstance,
                                   ItemStack stack,
                                   ItemDisplayContext context,
                                   BakedModelCache cache,
                                   RandomSource random) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) {
            return;
        }
        if (!(stack.getItem() instanceof ModularElytraItem)) {
            return;
        }
        TextureAtlasSprite sprite = getTrimSprite(trim);
        if (sprite == null) {
            return;
        }
        passes.add(new ElytraTrimPass(trim, holder, moduleInstance, stack, context, cache, random));
    }

    private static void resolveColorProvider(
            ClientEvents.MutableValue<SpriteColorer> ref,
            Material material,
            ItemStack stack,
            ItemDisplayContext displayMode
    ) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) {
            return;
        }
        if (!(stack.getItem() instanceof ModularElytraItem)) {
            return;
        }
        if (!displayMode.equals(ItemDisplayContext.HEAD)) {
            return;
        }
        TextureAtlasSprite sprite = getTrimSprite(trim);
        if (sprite == null) {
            return;
        }
        if (sprite.equals(((TextureAtlasAccessor) ETAtlasHolder.INSTANCE.getAtlas()).getMiapiMissingSprite())) {
            return;
        }
        ref.set(new SpriteOverlayer(material, sprite, ref.get()));
    }

    public static TextureAtlasSprite getTrimSprite(ArmorTrim trim) {
        ResourceLocation id = trim.pattern()
                .value()
                .assetId()
                .withPath(path ->
                        "trims/models/elytra/" +
                        path +
                        "_" +
                        trim.material().value().assetName()
                );

        return ETAtlasHolder.INSTANCE.getAtlas().getSprite(id);
    }
}