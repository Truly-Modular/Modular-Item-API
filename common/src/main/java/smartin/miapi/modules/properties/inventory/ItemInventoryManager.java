package smartin.miapi.modules.properties.inventory;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.inventory.features.*;
import smartin.miapi.modules.properties.inventory.impl.PlayerArmorSlotInfo;
import smartin.miapi.registries.DatapackMiapiRegistry;
import smartin.miapi.registries.MiapiRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemInventoryManager {
    public static final Map<ResourceLocation, SlotInfo> PLAYER_TO_SLOT = new HashMap<>();
    public static final PrioritizedEvent<GetInventoryFeatures> GET_INVENTORY_FEATURES_EVENT = PrioritizedEvent.createEventResult();
    public static final FacetKey<SimpleEntityFacet<List<InventoryInstance>>> CACHE = SimpleEntityFacet.createSimple(
                    Miapi.id("player_inv_holder"),
                    Codec.list(AutoCodec.of(InventoryInstance.class).codec()))
            .setDefaultValue(null)
            .setPredicate(Player.class::isInstance)
            .setSaveCondition(data -> false)
            .syncToClientsOnSet(false)
            .build();
    public static final MiapiRegistry<InventoryFeatureType> INVENTORY_FEATURE_REGISTRY = MiapiRegistry.getInstance(InventoryFeatureType.class);
    public static DatapackMiapiRegistry<InventoryType> INVENTORY_TYPE_REGISTRY = DatapackMiapiRegistry.getInstance(InventoryType.class);

    static {
        INVENTORY_FEATURE_REGISTRY.register(AutoPickupFeatureType.ID, AutoPickupFeatureType.FEATURE);
        INVENTORY_FEATURE_REGISTRY.register(BackPackViewFeatureType.ID, BackPackViewFeatureType.FEATURE);
        INVENTORY_FEATURE_REGISTRY.register(InventorySizeFeatureType.ID, InventorySizeFeatureType.FEATURE);
        INVENTORY_FEATURE_REGISTRY.register(TagBlacklistFeatureType.ID, TagBlacklistFeatureType.FEATURE);
        INVENTORY_FEATURE_REGISTRY.register(TagWhiteListFeatureType.ID, TagWhiteListFeatureType.FEATURE);
        INVENTORY_FEATURE_REGISTRY.register(InteractFromInventoryFeature.FEATURE.id(), InteractFromInventoryFeature.FEATURE);

        // Armor slots
        PLAYER_TO_SLOT.put(
                Miapi.id("armor_head"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.HEAD,
                        Miapi.id("armor_head"),
                        Color.YELLOW,
                        Component.literal("Helmet")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_chest"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.CHEST,
                        Miapi.id("armor_chest"),
                        Color.GREEN,
                        Component.literal("Chestplate")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_legs"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.LEGS,
                        Miapi.id("armor_legs"),
                        Color.BLUE,
                        Component.literal("Leggings")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_feet"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.FEET,
                        Miapi.id("armor_feet"),
                        Color.RED,
                        Component.literal("Boots")
                )
        );
    }

    public static List<InventoryInstance> getAllInventories(Player player) {
        if (true) {
            return computeNewInventoryInstances(player);
        }
        var optional = CACHE.getOptional(player);
        if (optional.isEmpty()) {
            return computeNewInventoryInstances(player);
        } else {
            if (optional.get().get() == null) {
                List<InventoryInstance> newList = computeNewInventoryInstances(player);
                optional.get().set(newList);
            }
            return optional.get().get();
        }
    }

    public static <T> Stream<InventoryInstance> getInventoriesWith(
            Player player,
            InventoryFeatureType<T> feature,
            Predicate<T> filter
    ) {
        return getAllInventories(player).stream()
                .filter(inv -> inv.getFeatures().has(feature) && filter.test(inv.getFeatures().get(feature).get()));
    }

    private static List<InventoryInstance> computeNewInventoryInstances(Player player) {
        List<InventoryInstance> result = new ArrayList<>();
        for (SlotInfo slot : PLAYER_TO_SLOT.values()) {
            ItemStack stack = slot.getStack(player);
            if (stack.isEmpty()) continue;
            INVENTORY_TYPE_REGISTRY.getFlatMap().forEach((id, type) -> {
                InventoryInstance instance = new InventoryInstance(player, stack, type, slot);
                if (instance.getSize() > 0) {
                    result.add(instance);
                }
            });
        }
        return result;
    }

    public interface GetInventoryFeatures {
        EventResult getFeatures(AtomicReference<InventoryFeatureType.FeatureSet> mutableSet, Player player, ItemStack itemStack, InventoryType inventoryType);
    }
}