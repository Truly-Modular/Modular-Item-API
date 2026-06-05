package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.InventoryComponent;
import smartin.miapi.modules.properties.inventory.InventoryType;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class GrowToFillStateFeatureType implements InventoryFeatureType<Boolean> {
    public static final GrowToFillStateFeatureType FEATURE = new GrowToFillStateFeatureType();
    public static final ResourceLocation QUICK_STORAGE_INV = Miapi.id("item_quick_storage");

    private GrowToFillStateFeatureType() {
        ItemInventoryManager.GET_INVENTORY_FEATURES_EVENT.register(new ItemInventoryManager.GetInventoryFeatures() {
            @Override
            public EventResult getFeatures(AtomicReference<FeatureSet> mutableSet, Player player, ItemStack itemStack, InventoryType inventoryType) {
                if (!itemStack.isEmpty()) {
                    if (mutableSet.get().get(FEATURE).orElse(false)) {
                        InventoryComponent component = itemStack.get(InventoryComponent.ITEM_INVENTORIES);
                        if (component != null) {
                            InventoryComponent.CachedContents cachedContents = component.inventories().getOrDefault(inventoryType.getId(), InventoryComponent.CachedContents.EMPTY);
                            int actualSize = -1;
                            for (int i = 0; i < cachedContents.getLookupCache().size(); i++) {
                                if (!cachedContents.getLookupCache().get(i).isEmpty()) {
                                    actualSize = i;
                                }
                            }
                            Map<InventoryFeatureType<?>, Object> features = new HashMap<>(mutableSet.get().features);
                            features.put(InventorySizeFeatureType.FEATURE, new DoubleOperationResolvable(actualSize + 1));
                            mutableSet.set(new FeatureSet(features));
                        }
                    }
                }
                return EventResult.pass();
            }
        });
    }

    public static final ResourceLocation ID =
            Miapi.id("set_to_size");

    @Override
    public Boolean merge(Boolean left, Boolean right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }

    @Override
    public Boolean initialize(Boolean property, ModuleInstance context) {
        return property;
    }
}