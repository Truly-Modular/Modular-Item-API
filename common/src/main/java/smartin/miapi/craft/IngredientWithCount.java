package smartin.miapi.craft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.crafting.Ingredient;

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
