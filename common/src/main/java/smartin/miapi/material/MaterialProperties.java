package smartin.miapi.material;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This property allows materials to set specific Properties
 */
public class MaterialProperties extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("material_property");
    public static MaterialProperties property;

    public MaterialProperties() {
        super(Codec.list(Codec.STRING));
        property = this;
        PropertyResolver.register("material_property", (moduleInstance, oldMap) -> {
            Material material = MaterialProperty.getMaterial(moduleInstance);
            Map<ModuleProperty<?>, Object> returnMap = new HashMap<>(oldMap);
            if (material != null) {
                List<String> keys = getData(moduleInstance).orElse(new ArrayList<>());
                if (keys.isEmpty()) {
                    keys = List.of("default");
                }
                if (moduleInstance.module != null) {
                    List<String> newKeys = new ArrayList<>();
                    newKeys.add(moduleInstance.module.id().toString());
                    newKeys.addAll(keys);
                    keys = newKeys;
                }
                for (String key : keys) {
                    Map<ModuleProperty<?>, Object> materialProperties = material.materialProperties(key);
                    if (!materialProperties.isEmpty()) {
                        returnMap = PropertyResolver.merge(oldMap, materialProperties, MergeType.SMART);
                    }
                }
            }
            return returnMap;
        });
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
