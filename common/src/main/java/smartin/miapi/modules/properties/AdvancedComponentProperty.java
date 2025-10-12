package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedComponentProperty extends CodecProperty<Map<ResourceLocation, AdvancedComponentProperty.ComponentData>>
        implements ComponentApplyProperty, CraftingProperty {

    public static final Codec<AdvancedComponentProperty.ComponentData> COMPONENT_CODEC = AutoCodec.of(ComponentData.class).codec();
    public static Codec<Map<ResourceLocation, ComponentData>> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, COMPONENT_CODEC);

    public static final ResourceLocation KEY = Miapi.id("advanced_components");
    public static AdvancedComponentProperty property;

    public AdvancedComponentProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {
        getData(itemStack).ifPresent(map -> map.forEach((id, compData) -> {
            try {
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                if (type == null) {
                    Miapi.LOGGER.error("Could not find Component Type " + id);
                    return;
                }
                update(type, compData, itemStack);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("Could not apply component " + id, e);
                Miapi.LOGGER.error("raw data " + compData);
            }
        }));
    }

    @Override
    public Map<ResourceLocation, AdvancedComponentProperty.ComponentData> initialize(Map<ResourceLocation, AdvancedComponentProperty.ComponentData> property, ModuleInstance context) {
        Map<ResourceLocation, AdvancedComponentProperty.ComponentData> map = new ConcurrentHashMap<>();
        property.forEach((id, element) -> {
            map.put(id, new AdvancedComponentProperty.ComponentData(element.id, deepParse(element.data, context, element.resolve()), element.resolve, element.overWrite));
        });
        return map;
    }

    public <T> void update(DataComponentType<T> type, ComponentData data, ItemStack itemStack) {
        var result = type.codec().decode(JsonOps.INSTANCE, data.data());
        if (result.isError()) {
            throw new RuntimeException("Could not decode Data Component " + type);
        }
        if ((itemStack.has(type) && !data.overWrite())) {
            return;
        }
        itemStack.set(type, result.getOrThrow().getFirst());
    }

    public JsonElement deepParse(JsonElement element, ModuleInstance context, boolean deepResolve) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            JsonObject next = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                next.add(entry.getKey(), deepParse(entry.getValue(), context, deepResolve));
            }
            return next;
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            JsonArray next = new JsonArray(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                next.add(deepParse(arr.get(i), context, deepResolve));
            }
            return next;
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && !deepResolve) {
            String potential = element.getAsString();
            if (potential.startsWith("|||miapi.evaluate")) {
                potential = potential.replace("|||miapi.evaluate", "");
                return new JsonPrimitive(StatResolver.resolveDouble(potential, context));
            }
            return element.deepCopy();
        } else {
            return element.deepCopy();
        }
    }

    @Override
    public Map<ResourceLocation, ComponentData> merge(Map<ResourceLocation, ComponentData> left, Map<ResourceLocation, ComponentData> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        getData(old).ifPresent(map -> map.forEach((id, compData) -> {
            try {
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                if (type == null) {
                    Miapi.LOGGER.error("Could not find Component Type " + id);
                    return;
                }
                if (compData.overWrite()) {
                    crafting.remove(type);
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("Could not remove component " + id, e);
                Miapi.LOGGER.error("raw data " + compData);
            }
        }));
        updateComponent(crafting, bench.getLevel().registryAccess());
        return crafting;
    }

    public record ComponentData(ResourceLocation id,
                                JsonElement data,
                                @CodecBehavior.Optional @AutoCodec.Name("overwrite") Boolean overWrite,
                                @CodecBehavior.Optional Boolean resolve) {
        public Boolean resolve() {
            return resolve == null || resolve;
        }

        public Boolean overWrite() {
            return overWrite == null ? Boolean.TRUE : overWrite;
        }
    }
}
