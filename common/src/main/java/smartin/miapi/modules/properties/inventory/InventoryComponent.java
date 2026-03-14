package smartin.miapi.modules.properties.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.HashMap;
import java.util.Map;

public record InventoryComponent(Map<ResourceLocation, ItemContainerContents> inventories) {

    public static final Codec<InventoryComponent> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, ItemContainerContents.CODEC)
                    .xmap(InventoryComponent::new, InventoryComponent::inventories);

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new,
                            ResourceLocation.STREAM_CODEC,
                            ItemContainerContents.STREAM_CODEC),
                    InventoryComponent::inventories,
                    InventoryComponent::new
            );

    public static final DataComponentType<InventoryComponent> ITEM_INVENTORIES =
            DataComponentType.<InventoryComponent>builder()
                    .persistent(InventoryComponent.CODEC)
                    .networkSynchronized(InventoryComponent.STREAM_CODEC)
                    .build();

    public static final InventoryComponent EMPTY = new InventoryComponent(new HashMap<>());
}
