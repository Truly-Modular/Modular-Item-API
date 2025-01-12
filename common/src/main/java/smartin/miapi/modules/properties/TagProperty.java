package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the Modules to define tags to identify eachother
 * @header Tag Property
 * @path /data_types/properties/tag
 * @description_start
 * The TagProperty allows modules to define and associate tags with themselves. These tags can be used
 * to categorize and identify modules based on their associated tags.
 *
 * Tags are represented as a list of strings and can be used to filter or group items and modules. For example,
 * you could use tags to identify items that belong to a certain category or have specific properties, or to
 * find modules that share common attributes.
 *
 * Tags are mostly meant to be targeted by synergies.
 * @description_end
 * @data tag: A list of strings representing the tags associated with an item or module.
 */

public class TagProperty extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("tag");
    public static TagProperty property;
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);

    public TagProperty() {
        super(CODEC);
        property = this;
    }


    public static List<String> getTags(ItemStack itemStack) {
        return property.getData(itemStack).orElse(new ArrayList<>());
    }

    public static List<String> getTags(ModuleInstance instance) {
        return property.getData(instance).orElse(new ArrayList<>());
    }

    public static List<String> getTags(ItemModule module) {
        return property.getData(module).orElse(new ArrayList<>());
    }

    public static List<ItemModule> getModulesWithTag(String tag) {
        List<ItemModule> modules = new ArrayList<>();
        RegistryInventory.modules.getFlatMap().forEach((key, module) -> {
            if (getTags(module).contains(tag)) modules.add(module);
        });
        return modules;
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
