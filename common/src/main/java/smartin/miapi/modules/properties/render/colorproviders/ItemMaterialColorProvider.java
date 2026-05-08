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

public class ItemMaterialColorProvider extends MaterialColorProvider {
    public Material material;
    public Material actualMaterial;
    public boolean needCheck = true;

    public ItemMaterialColorProvider() {
    }

    public ItemMaterialColorProvider(Material material) {
        this.material = material;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void getConsumer(TextureAtlasSprite sprite,
                            ItemStack stack,
                            ModuleInstance moduleInstance,
                            ItemDisplayContext mode,
                            VertexConsumerProvider out) {
        if (actualMaterial == null && needCheck) {
            actualMaterial = MaterialProperty.getMaterialFromIngredient(stack);
            needCheck = false;
        }
        if (actualMaterial != null) {
            actualMaterial.getRenderController(moduleInstance, mode).getVertexConsumer(sprite, stack, moduleInstance, mode, out);
        }
        material.getRenderController(moduleInstance, mode).getVertexConsumer(sprite, stack, moduleInstance, mode, out);
    }

    @Override
    public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
        Material material1 = MaterialProperty.getMaterialFromIngredient(stack);
        if (material1 != null) {
            return new smartin.miapi.modules.properties.render.colorproviders.ItemMaterialColorProvider(material1);
        }
        return new ModelColorProvider();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        smartin.miapi.modules.properties.render.colorproviders.ItemMaterialColorProvider that = (smartin.miapi.modules.properties.render.colorproviders.ItemMaterialColorProvider) obj;
        return java.util.Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(material);
    }

}
