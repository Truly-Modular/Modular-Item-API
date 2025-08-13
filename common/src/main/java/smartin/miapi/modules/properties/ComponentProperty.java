package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.pose.network.clientbound.PoseFacetSyncPacket;
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

public class ComponentProperty extends CodecProperty<Map<ResourceLocation, JsonElement>> implements ComponentApplyProperty, CraftingProperty {
    public static Codec<Map<ResourceLocation, JsonElement>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, StatResolver.Codecs.JSONELEMENT_CODEC);
    public static final ResourceLocation KEY = Miapi.id("components");
    public static ComponentProperty property;

    public ComponentProperty() {
        super(CODEC);
        property = this;
        PoseFacetSyncPacket poseFacetSyncPacket;
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {
        getData(itemStack).ifPresent(map -> {
            map.forEach((id, json) -> {
                try {
                    DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                    if (type == null) {
                        Miapi.LOGGER.error("could not find Component Type " + type);
                    } else {
                        update(type, json, itemStack);
                    }
                } catch (RuntimeException e) {
                    Miapi.LOGGER.error("Could not apply component " + id);
                    Miapi.LOGGER.error("raw data " + json);
                }
            });
        });
    }

    public Map<ResourceLocation, JsonElement> initialize(Map<ResourceLocation, JsonElement> property, ModuleInstance context) {
        Map<ResourceLocation, JsonElement> map = new ConcurrentHashMap<>();
        property.forEach((id, element) -> {
            map.put(id, deepParse(element, context));
        });
        return map;
    }

    public <T> void update(DataComponentType<T> type, JsonElement element, ItemStack itemStack) {
        var result = type.codec().decode(JsonOps.INSTANCE, element);
        if (result.isError()) {
            throw new RuntimeException("Could not decode Data Component ");
        }
        itemStack.set(type, result.getOrThrow().getFirst());
    }

    public JsonElement deepParse(JsonElement element, ModuleInstance context) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            JsonObject next = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                next.add(
                        entry.getKey(),
                        deepParse(entry.getValue(), context)
                );
            }
            return next;
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            JsonArray next = new JsonArray(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                next.add(arr.get(i));
            }
            return next;
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String potential = element.getAsString();
            if (potential.startsWith("|||miapi.evaluate")) {
                potential = potential.replace("|||miapi.evaluate","");
                return new JsonPrimitive(StatResolver.resolveDouble(potential,context));
            }
            return element.deepCopy();
        } else {
            return element.deepCopy();
        }
    }

    @Override
    public Map<ResourceLocation, JsonElement> merge(Map<ResourceLocation, JsonElement> left, Map<ResourceLocation, JsonElement> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        getData(old).ifPresent(map -> {
            map.forEach((id, json) -> {
                try {
                    DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                    if (type == null) {
                        Miapi.LOGGER.error("could not find Component Type " + type);
                    } else {
                        crafting.remove(type);
                    }
                } catch (RuntimeException e) {
                    Miapi.LOGGER.error("Could not apply component " + id);
                    Miapi.LOGGER.error("raw data " + json);
                }
            });
        });
        updateComponent(crafting, bench.getLevel().registryAccess());
        return crafting;
    }
}
