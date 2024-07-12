package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the Modules to define tags to identify eachother
 */
public class TagProperty extends CodecProperty<List<String>> {
    public static final String KEY = "tag";
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
        return ModuleProperty.mergeList(left, right, mergeType);
    }
}
