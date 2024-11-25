package smartin.miapi.craft;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @header Ingredient with Count
 * @path /data_types/ingredient_count
 * @description_start This is an expansions of vanillas default ingredient to also allow for a count, how much of that ingredient is required.
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
                    Codec.DOUBLE
                            .fieldOf("count")
                            .forGetter((countIngredient) -> countIngredient.count)
            ).apply(instance, IngredientWithCount::new
            ));
    public static final Codec<IngredientWithCount> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<IngredientWithCount, T>> decode(DynamicOps<T> ops, T input) {
            // Decode the ingredient with embedded count
            return Ingredient.CODEC.decode(ops, input).flatMap(pair -> {
                Ingredient ingredient = pair.getFirst();
                T ingredientData = pair.getSecond();

                // Extract the count from the ingredient
                return ops.getNumberValue(ops.get(ingredientData, "value").getOrThrow())
                        .map(count -> new IngredientWithCount(ingredient, count.doubleValue()))
                        .map(countIngredient -> Pair.of(countIngredient, input));
            });
        }

        @Override
        public <T> DataResult<T> encode(IngredientWithCount input, DynamicOps<T> ops, T prefix) {
            // Encode the ingredient with embedded count
            var result = Ingredient.CODEC.encode(input.ingredient, ops, prefix);
            ops.set(result.result().get(), "value", ops.createDouble(input.count));
            return result;
        }
    };

    public Ingredient ingredient;
    public double count;

    public IngredientWithCount(Ingredient ingredient, double count) {
        this.ingredient = ingredient;
        this.count = count;
    }
}
