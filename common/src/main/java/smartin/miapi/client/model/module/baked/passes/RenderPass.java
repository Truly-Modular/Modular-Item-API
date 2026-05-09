package smartin.miapi.client.model.module.baked.passes;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;

import java.util.ArrayList;
import java.util.List;

public interface RenderPass {
    void render(MiapiModel.RenderContext context,
                float[] colors,
                float alpha,
                int light
    );

    static RenderPass[] getRenderPasses(
            ModelHolder holder,
            ModuleInstance moduleInstance,
            ItemStack stack,
            ItemDisplayContext context,
            BakedModelCache cache,
            RandomSource random) {
        List<RenderPass> passes = new ArrayList<>();
        passes.add(new StandardRenderPass(moduleInstance, stack, context, holder, cache, random));
        if (holder.entityRendering()) {
            passes.add(new ReverseRenderPass(moduleInstance, stack, context, holder, cache, random));
        }
        if (MiapiConfig.getClientConfig().enchantingGlint.shouldRenderGlint()) {
            if (MiapiClient.CUSTOM_SHADER_LOADED) {
                passes.add(new MiapiGlintRenderPass(moduleInstance, stack, GlintProperty.property.getGlintSettings(moduleInstance, stack), context, holder, cache, random));
            } else {
                passes.add(new VanillaGlintRenderPass(moduleInstance, stack, GlintProperty.property.getGlintSettings(moduleInstance, stack), context, holder, cache, random));
            }
        }
        if (!MiapiConfig.getClientConfig().render.enableFastTrim) {
            passes.add(new TrimRenderPass(holder, cache, random));
        }
        return passes.toArray(new RenderPass[passes.size()]);
    }


}
