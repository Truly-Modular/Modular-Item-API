package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * @header Copy Item Lore
 * @path /data_types/properties/lore/copy_from_item
 * @description_start Copies lore properties from a specified item. This property enables the lore text to be duplicated
 * from the referenced item, allowing for a consistent narrative or flavor across items. Intended for items that share lore
 * changing lore.
 * @description_end
 * @data copy_item_lore:the id of the item to copy lore from
 */
public class AssumeItemIdentityProperty extends CodecProperty<List<Holder<Item>>> {
    public static ResourceLocation KEY = Miapi.id("assume_item_id");
    public static AssumeItemIdentityProperty property;
    public static WeakHashMap<ItemStack, Optional<List<Holder<Item>>>> lookupCache = new WeakHashMap<>();

    public AssumeItemIdentityProperty() {
        super(Miapi.toListOrSimple(BuiltInRegistries.ITEM.holderByNameCodec()));
        property = this;
        MiapiEvents.CLEAR_CACHE.register(() -> {
            lookupCache = new WeakHashMap<>();
            return EventResult.pass();
        });
    }

    public static @NotNull Boolean isItem(boolean original, Item item, ItemStack stack) {
        var property = getProperty(item, stack);
        var match = property.map(a -> a.stream().anyMatch(h -> h.value().equals(item)));
        return match.orElse(original);
    }

    private static Optional<List<Holder<Item>>> getProperty(Item item, ItemStack itemStack) {
        if (lookupCache.containsKey(itemStack)) {
            return lookupCache.get(itemStack);
        }
        Optional<List<Holder<Item>>> data = AssumeItemIdentityProperty.property.getData(itemStack);
        lookupCache.put(itemStack, data);
        return data;
    }


    @Override
    public List<Holder<Item>> merge(List<Holder<Item>> left, List<Holder<Item>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
