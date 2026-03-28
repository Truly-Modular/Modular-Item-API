package smartin.miapi.material.base;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.MutableModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * controls most of the Crafting logic of {@link Material}
 * {@link IngredientController#getMaterial(ModuleInstance, Map)} is the exception
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
     * This should be implemented as well.
     * while the api internals do not use this, previews via JEI or similar utilize Ingredient logic
     * @return
     */
    default Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }


    static Ingredient mergeIngredients(Stream<Ingredient> ingredients) {
        return Ingredient.of(
                ingredients
                        .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                        .filter(stack -> !stack.isEmpty())
        );
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
    default void setMaterial(MutableModuleInstance moduleInstance) {
    }

    /**
     * resolving a material from a module.
     * used to load additional data on a module instance, should be in the data part of the {@link ModuleInstance}
     *
     * @param moduleInstance
     * @param properties
     * @return
     */
    Material getMaterial(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties);

    /**
     * allowing the material to change based on the ingredient, to return a variant of itself
     *
     * @param ingredient
     * @return
     */
    Material getMaterialFromIngredient(ItemStack ingredient);
}
