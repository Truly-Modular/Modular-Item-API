package smartin.miapi.modules.properties;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * @header Copy Item Lore
 * @path /data_types/properties/lore/copy_from_item
 * @description_start Copies lore properties from a specified item. This property enables the lore text to be duplicated
 * from the referenced item, allowing for a consistent narrative or flavor across items. Intended for items that share lore
 * changing lore.
 * @description_end
 * @data copy_item_lore:the id of the item to copy lore from
 */
public class CopyItemLoreProperty extends CodecProperty<Holder<Item>> {
    public static ResourceLocation KEY = Miapi.id("copy_item_lore");
    public static CopyItemLoreProperty property;

    public CopyItemLoreProperty() {
        super(BuiltInRegistries.ITEM.holderByNameCodec());
        property = this;
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> {
            getData(itemStack).ifPresent(itemHolder -> {
                itemHolder.value().appendHoverText(itemStack, context, tooltip, tooltipType);
            });
        });
    }

    @Override
    public Holder<Item> merge(Holder<Item> left, Holder<Item> right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
