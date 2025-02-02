package smartin.miapi.material.base;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ModuleInstance;

/**
 * controls most of the Crafting logic of {@link Material}
 * {@link Material#getMaterial(ModuleInstance)} is the exception
 */
public interface IngredientController {
    /**
     * get Crafting Value of Item, each module has a value assosiated of how much value is needed
     *
     * @param ingredient
     * @return
     */
    double getValueOfItem(ItemStack ingredient);

    /**
     * how much repair is done for an ingredient.
     * usually we only differentiate between 0 and positive values.
     *
     * @param ingredient
     * @return
     */
    default double getRepairValueOfItem(ItemStack ingredient) {
        return getValueOfItem(ingredient);
    }

    /**
     * return null if itemstack is not assosiated with the material
     * lower priority wins
     * this is to decide what Material is assosiated with this Ingredient Itemstack
     */
    @Nullable
    Double getPriorityOfIngredientItem(ItemStack ingredient);

    /**
     * writting the material to the module instance.
     * used to write additional data to the module if needed
     *
     * @param moduleInstance
     */
    default void setMaterial(ModuleInstance moduleInstance) {
    }
}
