package smartin.miapi.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * The ModularItemStackConverter class provides a mechanism to convert an ItemStack into its modular version.
 * It utilizes a list of ModularConverter instances to perform the conversion.
 */
public class ModularItemStackConverter {

    /**
     * A list of ModularConverter instances that will be used to convert the ItemStack.
     */
    public static List<ModularConverter> converters = new ArrayList<>();

    /**
     * Converts the original ItemStack into its modular version by applying the conversions from the list of converters.
     *
     * @param original The original ItemStack to be converted.
     * @return The modular version of the ItemStack.
     */
    public static ItemStack getModularVersion(ItemStack original) {
        if (original.is(RegistryInventory.MIAPI_FORBIDDEN_TAG)) {
            return original;
        }
        ItemStack converted = original.copy();
        if (original.getItem().getDefaultInstance().has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            var oldEntries = original.getItem().getDefaultInstance().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers();
            converted.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, (old) -> new ItemAttributeModifiers(
                    old.modifiers().stream().filter(a -> !oldEntries.contains(a)).toList(),
                    old.showInTooltip()
            ));
        }
        for (ModularConverter converter : converters) {
            converted = converter.convert(converted);
        }
        if (ModularItem.isModularItem(converted)) {
            ComponentApplyProperty.updateItemStack(converted, null);
        }
        return converted;
    }

    /**
     * The interface for converting an ItemStack into its modular version.
     */
    public interface ModularConverter {

        /**
         * Converts the given ItemStack into its modular version.
         *
         * @param stack The ItemStack to be converted.
         * @return The modular version of the ItemStack.
         */
        ItemStack convert(ItemStack stack);
    }
}
