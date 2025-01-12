package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
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
import smartin.miapi.modules.properties.util.*;

import java.util.List;
import java.util.Map;

/**
 * @header Component Property
 * @path /data_types/properties/component
 * @description_start
 * This Property allows you to set any component. Whenever the module giving the Component, that component is also removed again.
 * If there was a Component set before, that component will be overwritten
 * Its a map of id to Component data in json format
 * @description_end
 * @data id:data.
 */
public class ComponentProperty extends CodecProperty<Map<ResourceLocation, JsonElement>> implements ComponentApplyProperty, CraftingProperty {
    public static Codec<Map<ResourceLocation, JsonElement>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, StatResolver.Codecs.JSONELEMENT_CODEC);
    public static final ResourceLocation KEY = Miapi.id("components");
    public static ComponentProperty property;

    public ComponentProperty() {
        super(CODEC);
        property = this;
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

    public <T> void update(DataComponentType<T> type, JsonElement element, ItemStack itemStack) {
        var result = type.codec().decode(JsonOps.INSTANCE, element);
        if (result.isError()) {
            throw new RuntimeException("Could not decode Data Component ");
        }
        itemStack.set(type, result.getOrThrow().getFirst());
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
