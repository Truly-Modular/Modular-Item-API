package smartin.miapi.modules.abilities.key;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.AbilityProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyBindAbilityManagerProperty extends CodecProperty<Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>>> {
    public static final String KEY = "keybind_ability_context";
    public static KeyBindAbilityManagerProperty property;

    public static final Codec<Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>>> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, AbilityProperty.CODEC);

    public KeyBindAbilityManagerProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> merge(
            Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> left,
            Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> right,
            MergeType mergeType
    ) {
        Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> merged = new LinkedHashMap<>(left);

        right.forEach((id, rightList) -> {
            List<AbilityProperty.AbilityContext<?>> leftList = merged.getOrDefault(id, List.of());
            List<AbilityProperty.AbilityContext<?>> result =
                    AbilityProperty.property.merge(leftList, rightList, mergeType);
            merged.put(id, result);
        });

        return merged;
    }

    @Override
    public Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> initialize(
            Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> map,
            ModuleInstance context
    ) {
        Map<ResourceLocation, List<AbilityProperty.AbilityContext<?>>> initialized = new LinkedHashMap<>();

        map.forEach((id, list) -> {
            initialized.put(id, AbilityProperty.property.initialize(list, context));
        });

        return initialized;
    }
}
