package smartin.miapi.modules.properties.tag;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows the Modules to define tags to identify each other and materials
 *
 * @header Tag Property
 * @path /data_types/properties/tag
 * @description_start The TagProperty allows modules to define and associate tags with themselves. These tags can be used
 * to categorize and identify modules based on their associated tags.
 * <p>
 * Tags are represented as a list of strings and can be used to filter or group items and modules. For example,
 * you could use tags to identify items that belong to a certain category or have specific properties, or to
 * find modules that share common attributes.
 * <p>
 * Material Properties also filter for these tags when looking how to apply
 * <p>
 * Tags are mostly meant to be targeted by synergies.
 * @description_end
 * @data module_tag: A list of strings representing the tags associated with an item or module.
 */
public class ModuleTagProperty extends CodecProperty<List<String>> {
    public static final ResourceLocation KEY = Miapi.id("module_tag");
    public static ModuleTagProperty property;
    public static Codec<List<String>> CODEC = Codec.list(Codec.STRING);

    public ModuleTagProperty() {
        super(CODEC);
        property = this;
        PropertyResolver.register("material_property", (moduleInstance, oldMap) -> {
            Material material = MaterialProperty.getMaterial(moduleInstance);
            Map<ModuleProperty<?>, Object> returnMap = new HashMap<>(oldMap);
            if (material != null) {
                List<String> keys = getTags(moduleInstance);
                if (keys.isEmpty()) {
                    keys = List.of("default");
                }
                if (moduleInstance.getModule() != null) {
                    List<String> newKeys = new ArrayList<>();
                    newKeys.add(moduleInstance.getModule().id().toString());
                    newKeys.addAll(keys);
                    keys = newKeys;
                }
                for (String key : keys) {
                    Map<ModuleProperty<?>, Object> materialProperties = PropertyResolver
                            .setSource(
                                    material.materialProperties(key),
                                    Component.translatable("miapi.property.source.material", material.getTranslation().getString()).withStyle(ChatFormatting.DARK_GRAY));
                    if (!materialProperties.isEmpty()) {
                        returnMap = PropertyResolver.merge(oldMap, materialProperties, MergeType.SMART);
                    }
                }
                Map<ModuleProperty<?>, Object> materialProperties = PropertyResolver
                        .setSource(
                                material.materialProperties(moduleInstance.moduleID.toString()),
                                Component.translatable("miapi.property.source.material", material.getTranslation().getString()).withStyle(ChatFormatting.DARK_GRAY));
                if (!materialProperties.isEmpty()) {
                    returnMap = PropertyResolver.merge(oldMap, materialProperties, MergeType.SMART);
                }
            }
            return returnMap;
        });
    }


    public static List<String> getTags(ItemStack itemStack) {
        List<String> tags = new ArrayList<>(property.getData(itemStack).orElse(new ArrayList<>()));
        tags.addAll(ModuleTagLegacyProperty.getTags(itemStack));
        tags.addAll(ModuleTagMaterialLegacyProperty.getTags(itemStack));
        return tags;
    }

    public static List<String> getTags(ModuleInstance instance) {
        List<String> tags = new ArrayList<>(property.getData(instance).orElse(new ArrayList<>()));
        tags.addAll(ModuleTagLegacyProperty.getTags(instance));
        tags.addAll(ModuleTagMaterialLegacyProperty.getTags(instance));
        return tags;
    }

    public static List<String> getTags(ItemModule module) {
        List<String> tags = new ArrayList<>(property.getData(module).orElse(new ArrayList<>()));
        tags.addAll(ModuleTagLegacyProperty.getTags(module));
        tags.addAll(ModuleTagMaterialLegacyProperty.getTags(module));
        return tags;
    }

    public static List<String> getTags(Map<ModuleProperty<?>, Object> properties) {
        List<String> tags = new ArrayList<>(getTags(properties,ModuleTagProperty.property));
        tags.addAll(getTags(properties,ModuleTagLegacyProperty.property));
        tags.addAll(getTags(properties,ModuleTagMaterialLegacyProperty.property));
        return tags;
    }

    public static List<String> getTags(Map<ModuleProperty<?>, Object> properties, CodecProperty<List<String>> property) {
        List<String> tags = (List<String>) properties.get(property);
        if(tags==null){
            return List.of();
        }
        return tags;
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
