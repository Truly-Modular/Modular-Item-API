package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the set Itemtags via a Properterty (relies on {@link ItemStack#is(TagKey)}
 *
 * @header Fake Item Tag Property
 * @path /data_types/properties/fake_item_tag
 * @description_start The FakeItemTagProperty allows you to manage custom item tags for items. This property can be used to assign fake or
 * placeholder tags to items, which can be useful for various in-game functionalities or for modding purposes. These tags
 * are stored as a list of strings, and the property provides methods to retrieve and check these tags.
 * <p>
 * For example, you can use this property to simulate item tags that do not exist in the default Minecraft item tag system.
 * @description_end
 * @data tags: A list of strings representing the fake tags assigned to the item.
 */
public class FakeItemTagProperty extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("fake_item_tag");
    public static FakeItemTagProperty property;
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);

    public FakeItemTagProperty() {
        super(CODEC);
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
        boolean dyeAble = false;
        List<String> list = property.getData(itemStack).orElse(new ArrayList<>());
        for (ModuleInstance instance : moduleInstance.allSubModules()) {
            Material material = MaterialProperty.getMaterial(instance);
            if (material != null && material.canBeDyed()) {
                dyeAble = true;
            }
        }
        if(dyeAble){
            list = new ArrayList<>(list);
            list.add("minecraft:dyeable");
        }
        return list;
    }

    public static boolean hasTag(ResourceLocation identifier, ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return getTags(itemStack).contains(identifier.toString());
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
