package smartin.miapi.item;

import net.minecraft.item.ItemStack;

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
        for (ModularConverter converter : converters) {
            original = converter.convert(original);
        }
        return original;
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
