package smartin.miapi.material.composite.material;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.material.DelegatingMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

/**
 * This Composite allows a material to be replaced with another material.
 *
 * @header Material Copy Composite
 * @description_start The Material Copy Composite replaces the current material with a specified base material.
 * This allows for direct substitution of one material with another, maintaining all properties
 * and behaviors of the target material.
 * @description_end
 * @path /data_types/composites/material_copy
 * @data material: The material that replaces the current material.
 */
public class MaterialPropertyMergeComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("material_merge_property");
    public static MapCodec<MaterialPropertyMergeComposite> MAP_CODEC = CompositeFromOtherMaterial.getEmptyCodec(MaterialPropertyMergeComposite::new);

    public MaterialPropertyMergeComposite() {
        super();
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        return new DelegatingMaterial(parent) {
            @Override
            public Map<ModuleProperty<?>, Object> materialProperties(String key) {
                return PropertyResolver.merge(parent.materialProperties(key), material.materialProperties(key), MergeType.SMART);
            }

            @Override
            public Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
                return PropertyResolver.merge(parent.getDisplayMaterialProperties(key), material.getDisplayMaterialProperties(key), MergeType.SMART);
            }


            @Override
            public List<String> getAllPropertyKeys() {
                Set<String> keys = new HashSet<>(parent.getAllPropertyKeys());
                keys.addAll(material.getAllPropertyKeys());
                return new ArrayList<>(keys);
            }

            @Override
            public List<String> getAllDisplayPropertyKeys() {
                Set<String> keys = new HashSet<>(parent.getAllDisplayPropertyKeys());
                keys.addAll(material.getAllDisplayPropertyKeys());
                return new ArrayList<>(keys);
            }

            @Override
            public Map<String, Map<ModuleProperty<?>, Object>> getHiddenProperty() {
                Map<String, Map<ModuleProperty<?>, Object>> parentHidden = parent.getHiddenProperty();
                Map<String, Map<ModuleProperty<?>, Object>> materialHidden = material.getHiddenProperty();
                return mergeProperties(parentHidden, materialHidden);
            }

            @Override
            public Map<String, Map<ModuleProperty<?>, Object>> getDisplayProperty() {
                Map<String, Map<ModuleProperty<?>, Object>> parentDisplay = parent.getDisplayProperty();
                Map<String, Map<ModuleProperty<?>, Object>> materialDisplay = material.getDisplayProperty();
                return mergeProperties(parentDisplay, materialDisplay);
            }

            @Override
            public Map<String, Map<ModuleProperty<?>, Object>> getActualProperty() {
                Map<String, Map<ModuleProperty<?>, Object>> parentActual = parent.getActualProperty();
                Map<String, Map<ModuleProperty<?>, Object>> materialActual = material.getActualProperty();
                return mergeProperties(parentActual, materialActual);
            }

            private Map<String, Map<ModuleProperty<?>, Object>> mergeProperties(
                    Map<String, Map<ModuleProperty<?>, Object>> parentProps,
                    Map<String, Map<ModuleProperty<?>, Object>> materialProps) {
                Map<String, Map<ModuleProperty<?>, Object>> merged = new HashMap<>();

                Set<String> keys = new HashSet<>(parentProps.keySet());
                keys.addAll(materialProps.keySet());

                for (String key : keys) {
                    Map<ModuleProperty<?>, Object> parentMap = parentProps.getOrDefault(key, Collections.emptyMap());
                    Map<ModuleProperty<?>, Object> materialMap = materialProps.getOrDefault(key, Collections.emptyMap());
                    merged.put(key, PropertyResolver.merge(parentMap, materialMap, MergeType.SMART));
                }
                return merged;
            }
        };

    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialPropertyMergeComposite that = (MaterialPropertyMergeComposite) obj;
        return overWriteAble == that.overWriteAble &&
               Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, overWriteAble);
    }
}
