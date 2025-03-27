package smartin.miapi.modules.properties;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

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

    public AssumeItemIdentityProperty() {
        super(Miapi.toListOrSimple(BuiltInRegistries.ITEM.holderByNameCodec()));
        property = this;
    }

    @Override
    public List<Holder<Item>> merge(List<Holder<Item>> left, List<Holder<Item>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
