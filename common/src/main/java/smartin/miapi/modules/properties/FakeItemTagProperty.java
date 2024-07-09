package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the set Itemtags via a Properterty (relies on {@link ItemStack#is(TagKey)}
 */
public class FakeItemTagProperty extends CodecBasedProperty<List<String>> {
    public static final String KEY = "fake_item_tag";
    public static FakeItemTagProperty property;
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);

    public FakeItemTagProperty() {
        super(CODEC);
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        return property.getData(itemStack).orElse(new ArrayList<>());
    }

    public static boolean hasTag(ResourceLocation identifier, ItemStack itemStack) {
        return getTags(itemStack).contains(identifier.toString());
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }
}
