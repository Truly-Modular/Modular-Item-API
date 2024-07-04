package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.Miapi;

import java.util.List;
import java.util.Optional;

public record ItemInInventoryCondition(Ingredient item, MinMaxBounds.Ints count) implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<Ingredient, T> result = Ingredient.CODEC.decode(ops, ops.getMap(input).getOrThrow().get("material")).getOrThrow();
            MinMaxBounds.Ints count =  MinMaxBounds.Ints.CODEC.parse(ops, input).getOrThrow();
            return DataResult.success(new Pair(new ItemInInventoryCondition(result.getFirst(), count), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };


    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Player> playerOptional = conditionContext.getContext(ConditionManager.PLAYER_LOCATION_CONTEXT);
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