package smartin.miapi.item;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModularItemStackConverter {

    public static List<ModularConverter> converters = new ArrayList<>();

    public static ItemStack getModularVersion(ItemStack original) {
        for (ModularConverter converter : converters) {
            original = converter.convert(original);
        }

        return original;
    }

    public interface ModularConverter {
        ItemStack convert(ItemStack stack);
    }
}
