package smartin.miapi.modules.properties.tag;

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

public class ModuleTagLegacyProperty extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("tag");
    public static ModuleTagLegacyProperty property;
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);

    public ModuleTagLegacyProperty() {
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
        RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.getFlatMap().forEach((key, module) -> {
            if (getTags(module).contains(tag)) modules.add(module);
        });
        return modules;
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
