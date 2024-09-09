package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @header Tag Condition
 * @description_start this condition checks if the associated module has a specific module tag. Not to be confused with Item Tags
 * @desciption_end
 * @path /data_types/condition/tag
 * @data type:tag
 * @data tag:the tag to be checked
 */
public class TagCondition implements ModuleCondition {
    public static Codec<TagCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("tag")
                            .forGetter(condition -> condition.tag),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, TagCondition::new));
    public String tag;
    Component error;


    public TagCondition(String tag, Component component) {
        this.tag = tag;
        error = component;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Map<ModuleProperty<?>, Object>> propertyMapOptional = conditionContext.getContext(ConditionManager.MODULE_PROPERTIES);
        if (propertyMapOptional.isPresent()) {
            Map<ModuleProperty<?>, Object> propertyMap = propertyMapOptional.get();
            List<String> tags = (List<String>) propertyMap.get(TagProperty.property);
            if (tags != null) {
                if (tags.contains(tag)) {
                    return true;
                }
            }
            conditionContext.failReasons.add(error);
        }
        return false;
    }
}
