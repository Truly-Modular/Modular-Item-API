package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.Miapi;

import java.util.List;
import java.util.Optional;
/**
 * @header Item in inventory Condition
 * @description_start
 * this condition checks if a certain amount of a certain item is in a players inventory
 * @desciption_end
 * @path /data_types/condition/item_in_inventory
 * @data type:item_in_inventory
 * @data item:Item in the form of an Ingredient
 * @data count:the amount of items needed to be present
 */
public record ItemInInventoryCondition(Ingredient item, MinMaxBounds.Ints count,
                                       Component error) implements ModuleCondition {

    public static Codec<ItemInInventoryCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Ingredient.CODEC.fieldOf("material")
                            .forGetter((condition) -> condition.item),
                    MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("count", MinMaxBounds.Ints.atLeast(1))
                            .forGetter((condition) -> condition.count),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.literal("Unavailable."))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, ItemInInventoryCondition::new));


    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Player> playerOptional = conditionContext.getContext(ConditionManager.PLAYER_CONTEXT);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            List<Component> reasons = conditionContext.failReasons;
            if (player != null && count.matches(getCount(player.getInventory(), item))) return true;

            Component text;
            int min = count.min().orElse(0);
            Integer max = count.max().orElse(0);
            String ingredientName = "";
            if (item.getItems() != null && item.getItems().length > 1) {
                ingredientName = Component.translatable(item.getItems()[0].getItem().getDescriptionId()).toString();
            }

            if (max != null)
                text = Component.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.specific", min, max, ingredientName);
            else
                text = Component.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.no_max", min, ingredientName);

            reasons.add(text);
        }
        return false;
    }

    public int getCount(Container inventory, Ingredient ingredient) {
        int found = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ingredient.test(inventory.getItem(i))) {
                found += inventory.getItem(i).getCount();
            }
        }
        return found;
    }
}