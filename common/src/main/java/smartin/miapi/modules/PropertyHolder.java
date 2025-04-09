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
            a -> StatResolver.Codecs.JSONELEMENT_CODEC.xmap(a::decodeAndLoad,
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

    public void load(ResourceLocation id) {
        replace.forEach((p, data) -> {
            try {
                p.load(id, p.encodeCast(data), true);
            } catch (RuntimeException e) {

            } catch (Exception e) {
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
