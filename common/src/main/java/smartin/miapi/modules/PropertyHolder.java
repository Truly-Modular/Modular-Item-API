package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.stream.Collectors;

public class PropertyHolder {
    public static final Codec<ModuleProperty<?>> PROPERTY_CODEC =
            Miapi.ID_CODEC.xmap(
                    (r) -> {
                        var p = RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.get(r);
                        if (p == null) {
                            try {
                                throw new DecoderException("could not find property " + r);
                            } catch (DecoderException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return p;
                    },
                    RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY::findKey);
    private static final Codec<Map<ModuleProperty<?>, Object>> PROPERTY_MAP_CODEC = Codec.dispatchedMap(
            PROPERTY_CODEC,
            a -> StatResolver.Codecs.JSONELEMENT_CODEC.xmap(a::decode,
                    a::encodeCast));

    public static final MapCodec<PropertyHolder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PROPERTY_MAP_CODEC.optionalFieldOf("replace", new HashMap<>()).forGetter(PropertyHolder::getReplace),
            PROPERTY_MAP_CODEC.optionalFieldOf("merge", new HashMap<>()).forGetter(PropertyHolder::getMerge),
            Codec.list(PROPERTY_CODEC).optionalFieldOf("remove", new ArrayList<>()).forGetter(PropertyHolder::getRemove)
    ).apply(instance, PropertyHolder::new));

    private final Map<ModuleProperty<?>, Object> replace;
    private final Map<ModuleProperty<?>, Object> merge;
    private final List<ModuleProperty<?>> remove;

    public PropertyHolder() {
        this(new HashMap<>(), new HashMap<>(), new ArrayList<>());
    }

    public PropertyHolder(Map<ModuleProperty<?>, Object> replace, Map<ModuleProperty<?>, Object> merge, List<ModuleProperty<?>> remove) {
        this.replace = replace;
        this.merge = merge;
        this.remove = remove;
    }

    public Map<ModuleProperty<?>, Object> getReplace() {
        return replace;
    }

    public Map<ModuleProperty<?>, Object> getMerge() {
        return merge;
    }

    public List<ModuleProperty<?>> getRemove() {
        return remove;
    }

    public static PropertyHolder decodePropertyHolder(JsonObject entryData, ResourceLocation source) {
        PropertyHolder propertyHolder = new PropertyHolder();

        // Handle replace/properties field
        JsonElement replaceProperty = entryData.get("replace");
        if (entryData.has("properties")) {
            replaceProperty = entryData.get("properties");
            Miapi.LOGGER.warn("The raw use of the Field `properties` should be replaced with the field `replace` in " + source);
        }
        propertyHolder.replace.putAll(decodeProperties(replaceProperty, source, "replace"));

        // Handle merge and remove fields
        propertyHolder.merge.putAll(decodeProperties(entryData.get("merge"), source, "merge"));
        propertyHolder.remove.addAll(decodeRemoveProperties(entryData.get("remove"), source));

        return propertyHolder;
    }

    public Map<ModuleProperty<?>, Object> applyHolder(Map<ModuleProperty<?>, Object> oldMap) {
        remove.forEach(oldMap::remove);
        merge.forEach((key, value) -> {
            if (oldMap.containsKey(key)) {
                oldMap.put(key, ItemModule.merge(key, oldMap.get(key), value, MergeType.SMART));
            } else {
                oldMap.put(key, value);
            }
        });
        oldMap.putAll(replace);
        return oldMap;
    }

    private static Map<ModuleProperty<?>, Object> decodeProperties(@Nullable JsonElement element, ResourceLocation source, String context) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) {
            return new HashMap<>();
        }

        Map<ModuleProperty<?>, Object> properties = new HashMap<>();
        element.getAsJsonObject().entrySet().forEach(propertyEntry -> {
            String propertyKey = propertyEntry.getKey();
            ModuleProperty<?> property = RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.get(Miapi.id(propertyKey));
            if (property == null) {
                logPropertyError(propertyKey, context, source, element);
                return;
            }

            try {
                if (property.load(source, propertyEntry.getValue(), Environment.isClient())) {
                    properties.put(property, property.decode(propertyEntry.getValue()));
                }
            } catch (Exception e) {
                logPropertyError(propertyKey, context, source, element, e);
            }
        });
        return properties;
    }

    private static void logPropertyError(String propertyKey, String context, ResourceLocation source, JsonElement element) {
        logPropertyError(propertyKey, context, source, element, null);
    }

    private static void logPropertyError(String propertyKey, String context, ResourceLocation source, JsonElement element, Exception e) {
        String errorMsg = "Could not find Property " + propertyKey + " in context " + context + " from source " + source;
        if (e != null) {
            Miapi.LOGGER.error(errorMsg, e);
        } else {
            Miapi.LOGGER.warn(errorMsg);
        }
        Miapi.LOGGER.error(Miapi.gson.toJson(element));
    }

    private static List<ModuleProperty<?>> decodeRemoveProperties(JsonElement element, ResourceLocation source) {
        if (element == null || !element.isJsonArray()) {
            return new ArrayList<>();
        }

        return element.getAsJsonArray().asList().stream()
                .filter(JsonElement::isJsonPrimitive)
                .map(e -> {
                    String key = e.getAsString();
                    return (ModuleProperty<?>) RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.get(Miapi.id(key));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
