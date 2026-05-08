package smartin.miapi.modules.properties.render.colorproviders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;

public class ParentColorProvider extends MaterialColorProvider {

    public ParentColorProvider() {
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void getConsumer(TextureAtlasSprite sprite,
                            ItemStack stack,
                            ModuleInstance moduleInstance,
                            ItemDisplayContext mode,
                            VertexConsumerProvider out) {
        material.getRenderController(moduleInstance, mode).getVertexConsumer(sprite, stack, moduleInstance, mode,out);
    }

    @Override
    public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
        Material material1 = MaterialProperty.getMaterial(instance);
        if (material1 != null) {
            return new MaterialColorProvider(material1, trimMode);
        }
        return new ModelColorProvider();
    }

    public ModuleInstance adapt(ModuleInstance moduleInstance) {
        if (moduleInstance.getParent() != null) {
            return moduleInstance.getParent();
        }
        return moduleInstance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        smartin.miapi.modules.properties.render.colorproviders.ParentColorProvider that = (smartin.miapi.modules.properties.render.colorproviders.ParentColorProvider) obj;
        return java.util.Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(material);
    }

}
