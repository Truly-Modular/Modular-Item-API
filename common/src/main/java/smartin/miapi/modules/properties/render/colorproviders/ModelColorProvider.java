package smartin.miapi.modules.properties.render.colorproviders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.modules.ModuleInstance;

public class ModelColorProvider implements ColorProvider {
//        ItemStack stack = ItemStack.EMPTY;

    public ModelColorProvider() {
    }

    public ModelColorProvider(ItemStack stack) {
//            this.stack = stack;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void getConsumer(TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode, VertexConsumerProvider out) {
        out.clean();
        out.vanillaVCGetter = (b) -> b.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
        out.getRenderSaveVC = (b) -> b.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
    }

    @Override
    public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
        return new smartin.miapi.modules.properties.render.colorproviders.ModelColorProvider(stack);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
