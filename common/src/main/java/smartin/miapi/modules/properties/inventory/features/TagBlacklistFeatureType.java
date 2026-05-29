package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

public class TagBlacklistFeatureType implements InventoryFeatureType<List<TagKey<Item>>> {

    public static final TagBlacklistFeatureType FEATURE = new TagBlacklistFeatureType();

    private TagBlacklistFeatureType() {

    }

    public static final ResourceLocation ID =
            Miapi.id("contents_blacklist");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<List<TagKey<Item>>> codec() {
        return Miapi.toListOrSimple(TagKey.codec(Registries.ITEM));
    }

    @Override
    public boolean allows(ItemStack stack, List<TagKey<Item>> featureData) {
        for (TagKey<Item> tag : featureData) {
            if (stack.is(tag)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<TagKey<Item>> initialize(List<TagKey<Item>> property, ModuleInstance context) {
        return property;
    }

    @Override
    public List<TagKey<Item>> merge(List<TagKey<Item>> left, List<TagKey<Item>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}