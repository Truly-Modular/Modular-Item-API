package smartin.miapi.modules.properties.inventory;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.entity.EntityHelper;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.features.InventoryFeatureType;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryFeatureProperty extends CodecProperty<Map<ResourceLocation, InventoryFeatureType.FeatureSet>> {
    public static ResourceLocation KEY = Miapi.id("inventory_features");
    public static InventoryFeatureProperty property = new InventoryFeatureProperty();

    protected InventoryFeatureProperty() {
        super(Codec.unboundedMap(ResourceLocation.CODEC, InventoryFeatureType.FeatureSet.CODEC));
        ItemInventoryManager.GET_INVENTORY_FEATURES_EVENT.register(new ItemInventoryManager.GetInventoryFeatures() {
            @Override
            public EventResult getFeatures(AtomicReference<InventoryFeatureType.FeatureSet> mutableSet, Player player, ItemStack itemStack, InventoryType inventoryType) {
                EntityHelper.getEquipedNonHandItems(player).forEach(stack -> {
                    getData(stack).ifPresent(map -> {
                        map.forEach((id, featuresSet) -> {
                            if (inventoryType.getId().equals(id)) {
                                mutableSet.set(mutableSet.get().merge(featuresSet, mutableSet.get(), MergeType.SMART));
                            }
                        });
                    });
                });
                return EventResult.pass();
            }
        });
    }

    @Override
    public Map<ResourceLocation, InventoryFeatureType.FeatureSet> merge(Map<ResourceLocation, InventoryFeatureType.FeatureSet> left, Map<ResourceLocation, InventoryFeatureType.FeatureSet> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (k, l, r) -> l.merge(l, r, mergeType));
    }

    @Override
    public Map<ResourceLocation, InventoryFeatureType.FeatureSet> initialize(Map<ResourceLocation, InventoryFeatureType.FeatureSet> right, ModuleInstance context) {
        Map<ResourceLocation, InventoryFeatureType.FeatureSet> init = new HashMap<>();
        right.forEach((id, features) -> {
            init.put(id, features.initialize(features, context));
        });
        return init;
    }

}
