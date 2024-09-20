package smartin.miapi.modules.properties;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Optional;

/**
 * @header Display Name Property
 * @path /data_types/properties/display_name
 * @description_start
 * The DisplayNameProperty enables the customization of item display names based on module data. It retrieves the display name
 * by resolving it through module instances and materials. The display name can be dynamically generated using translation keys
 * and material-specific translations.
 * @description_end
 * @data display_name: A Text element representing the display name of the item.
 *
 * @see CodecProperty
 * @see Component
 * @see Material
 * @see MaterialProperty
 * @see ModularItemCache
 */

public class DisplayNameProperty extends CodecProperty<Component> {
    public static final ResourceLocation KEY = Miapi.id("display_name");
    public static DisplayNameProperty property;

    public DisplayNameProperty() {
        super(ComponentSerialization.CODEC);
        property = this;
        ModularItemCache.setSupplier(KEY.toString(), DisplayNameProperty::resolveDisplayText);
    }

    public static Component getDisplayText(ItemStack stack) {
        return ModularItemCache.getVisualOnlyCache(stack, KEY.toString(), Component.empty());
    }

    private static Component resolveDisplayText(ItemStack itemStack) {
        Component name = Component.translatable("miapi.name.missing.nomodule");
        ModuleInstance root = ItemModule.getModules(itemStack);
        for (ModuleInstance moduleInstance : root.allSubModules()) {
            Optional<Component> componentOptional = property.getData(moduleInstance);
            if (componentOptional.isPresent()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    Component materialTranslation = material.getTranslation();
                    name = Component.translatable(componentOptional.get().getString(), materialTranslation);
                } else {
                    name = Component.translatable(componentOptional.get().getString(), "");
                }
            }
        }
        return name;
    }

    @Override
    public Component merge(Component left, Component right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }
}
