package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.Miapi;

import java.util.List;
import java.util.Optional;

public class ItemInInventoryCondition implements ModuleCondition {
    public Ingredient item;
    public MinMaxBounds.Ints count = MinMaxBounds.Ints.atLeast(1);

    public ItemInInventoryCondition() {
    }

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

    @Override
    public ModuleCondition load(JsonElement element) {
        try {
            JsonObject object = element.getAsJsonObject();
            if (!object.has("item"))
                throw new RuntimeException("Expected key 'item' for ItemInInventoryCondition, but it was not found.");
            Ingredient item = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("item")).getOrThrow();

            ItemInInventoryCondition condition = new ItemInInventoryCondition();
            condition.item = item;

            JsonElement countElement = object.get("count");
            if (countElement != null) {
                condition.count = MinMaxBounds.Ints.CODEC.parse(JsonOps.INSTANCE, countElement).getOrThrow();
            }

            return condition;
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not load ItemInInventoryCondition ", e);
        }
        return new TrueCondition();
    }
}