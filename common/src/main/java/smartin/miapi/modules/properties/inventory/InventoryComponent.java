package smartin.miapi.modules.properties.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record InventoryComponent(Map<ResourceLocation, CachedContents> inventories) {

    public static final Codec<InventoryComponent> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, CachedContents.CODEC)
                    .xmap(InventoryComponent::new, InventoryComponent::inventories);

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new,
                            ResourceLocation.STREAM_CODEC,
                            CachedContents.STREAM_CODEC),
                    InventoryComponent::inventories,
                    InventoryComponent::new
            );

    public static final DataComponentType<InventoryComponent> ITEM_INVENTORIES =
            DataComponentType.<InventoryComponent>builder()
                    .persistent(InventoryComponent.CODEC)
                    .networkSynchronized(InventoryComponent.STREAM_CODEC)
                    .build();

    public static final InventoryComponent EMPTY = new InventoryComponent(new HashMap<>());

    public static class CachedContents {
        public static CachedContents EMPTY = new CachedContents(ItemContainerContents.EMPTY);
        public static Codec<CachedContents> CODEC = ItemContainerContents.CODEC.xmap(CachedContents::new, cachedContents -> cachedContents.containerContents);
        public static StreamCodec<RegistryFriendlyByteBuf, CachedContents> STREAM_CODEC = ItemContainerContents.STREAM_CODEC.map(CachedContents::new, cachedContents -> cachedContents.containerContents);
        public ItemContainerContents containerContents;
        public List<ItemStack> lookupCache;

        public CachedContents(ItemContainerContents containerContents) {
            this.containerContents = containerContents;
        }

        public List<ItemStack> getLookupCache() {
            if (lookupCache == null) {
                lookupCache = containerContents.stream().toList();
            }
            return lookupCache;
        }
    }
}
