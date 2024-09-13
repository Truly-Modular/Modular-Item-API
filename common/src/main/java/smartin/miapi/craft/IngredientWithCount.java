package smartin.miapi.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;
/**
 * @header Ingredient with Count
 * @path /datatypes/ingredient_count
 * @description_start
 * This is an expansions of vanillas default ingredient to also allow for a count, how much of that ingredient is required.
 * You can check [Minecrafts Wiki](https://minecraft.wiki/w/Recipe#JSON_Format) for more information on the ingredient structure.
 * @description_end
 * @data ingredient: Same as minecrafts default ingredient, check datapack recipes for more information.
 * @data count: a simple integer, how much of the ingredient is actually needed.
 */
public class IngredientWithCount {
    public static Codec<IngredientWithCount> INGREDIENT_WITH_COUNT = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC
                            .fieldOf("ingredient")
                            .forGetter((countIngredient) -> countIngredient.ingredient),
                    Codec.INT
                            .fieldOf("count")
                            .forGetter((countIngredient) -> countIngredient.count)
            ).apply(instance, IngredientWithCount::new
            ));
    public Ingredient ingredient;
    public int count;

    public IngredientWithCount(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }
}
