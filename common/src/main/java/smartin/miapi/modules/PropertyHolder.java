package smartin.miapi.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

public class PropertyHolder {
    public static final Codec<ModuleProperty<?>> PROPERTY_CODEC =
            Miapi.ID_CODEC.xmap(
                    r -> {
                        ModuleProperty<?> p = RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.get(r);
                        if (p == null) {
                            throw new RuntimeException(new DecoderException("Could not find property for key: " + r));
                        }
                        return p;
                    },
                    property -> {
                        var key = RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.findKey(property);
                        if (key == null) {
                            throw new RuntimeException("Could not find registry key for property: " + property);
                        }
                        return key;
                    }
            );

    public static final Codec<Map<ModuleProperty<?>, Object>> PROPERTY_MAP_CODEC = Codec.dispatchedMap(
            PROPERTY_CODEC,
            a -> {
                if (a == null) {
                    throw new IllegalArgumentException("ModuleProperty codec (a) must not be null");
                }
                return StatResolver.Codecs.JSONELEMENT_CODEC.xmap(
                        json -> {
                            try {
                                if (json == null) {
                                    throw new IllegalArgumentException("JSON element to decode is null");
                                }
                                return a.decodeAndLoad(json);
                            } catch (RuntimeException e) {
                                throw new RuntimeException("Failed to decode and load JSON for property: " + a + " " + e.getMessage(), e);
                            }
                        },
                        obj -> {
                            try {
                                if (obj == null) {
                                    throw new IllegalArgumentException("Object to encode is null");
                                }
                                return a.encodeCast(obj);
                            } catch (RuntimeException e) {
                                throw new RuntimeException("Failed to encode object for property: " + a, e);
                            }
                        }
                );
            }
    );


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

    public void load(ResourceLocation id) {
        replace.forEach((p, data) -> {
            try {
                p.load(id, p.encodeCast(data), true);
            } catch (Exception ignored) {
            }
        });
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

    public Map<ModuleProperty<?>, Object> applyHolder(Map<ModuleProperty<?>, Object> oldMap, Optional<Component> component) {
        remove.forEach(oldMap::remove);
        PropertyResolver.setSource(merge, component).forEach((key, value) -> {
            if (oldMap.containsKey(key)) {
                oldMap.put(key, ItemModule.merge(key, oldMap.get(key), value, MergeType.SMART));
            } else {
                oldMap.put(key, value);
            }
        });
        oldMap.putAll(PropertyResolver.setSource(replace, component));
        return oldMap;
    }
}
