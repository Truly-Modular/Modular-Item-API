package smartin.miapi.modules.properties.render.colorproviders;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;

public class MaterialColorProvider extends TrimColorProvider {
    public Material material;

    public MaterialColorProvider() {
        super(TrimRenderer.TrimMode.NONE);
    }

    public MaterialColorProvider(Material material, TrimRenderer.TrimMode mode) {
        super(mode);
        this.material = material;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public VertexConsumer getConsumer(MultiBufferSource vertexConsumers,
                                      TextureAtlasSprite sprite,
                                      ItemStack stack,
                                      ModuleInstance moduleInstance,
                                      ItemDisplayContext mode) {
        MaterialRenderController controller = material.getRenderController(moduleInstance, mode);
        controller = getTrimController(controller, material, stack,mode);
        return controller.getVertexConsumer(vertexConsumers, sprite, stack, moduleInstance, mode);
    }

    @Override
    public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
        Material material1 = MaterialProperty.getMaterial(instance);
        if (material1 != null) {
            return new smartin.miapi.modules.properties.render.colorproviders.MaterialColorProvider(material1, trimMode);
        }
        return new ModelColorProvider();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        smartin.miapi.modules.properties.render.colorproviders.MaterialColorProvider that = (smartin.miapi.modules.properties.render.colorproviders.MaterialColorProvider) obj;
        return java.util.Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(material);
    }

}
