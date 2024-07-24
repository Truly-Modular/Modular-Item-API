package smartin.miapi.modules.material;

import com.mojang.serialization.Codec;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.properties.util.CodecProperty;
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
    public static String KEY = "material_property";
    public static MaterialProperties property;

    public MaterialProperties() {
        super(Codec.list(Codec.STRING));
        property = this;
        PropertyResolver.register("material_property", (moduleInstance, oldMap) -> {
            Material material = MaterialProperty.getMaterial(oldMap);
            Map<ModuleProperty<?>, Object> returnMap = new HashMap<>(oldMap);
            if (material != null) {
                List<String> keys = getData(moduleInstance).orElse(new ArrayList<>());
                if (keys.isEmpty()) {
                    keys = List.of("default");
                }
                if (moduleInstance.module != null) {
                    List<String> newKeys = new ArrayList<>();
                    newKeys.add(moduleInstance.module.name());
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
        return ModuleProperty.mergeList(left, right, mergeType);
    }
}
