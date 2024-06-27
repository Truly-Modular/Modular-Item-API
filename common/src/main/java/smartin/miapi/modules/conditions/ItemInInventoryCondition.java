package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.Miapi;

import java.util.List;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemInInventoryCondition implements ModuleCondition {
    public Ingredient item;
    public MinMaxBounds.Ints count = MinMaxBounds.Ints.atLeast(1);

    public ItemInInventoryCondition() {
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Player player = moduleConditionContext.player;
            List<Component> reasons = moduleConditionContext.reasons;
            if (player != null && count.matches(getCount(player.getInventory(), item))) return true;

            Component text;

            int min = count.getMin() == null ? 0 : count.getMin();
            Integer max = count.getMax();
            String ingredientName = "";
            if(item.getItems()!=null && item.getItems().length>1){
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
            Ingredient item = Ingredient.fromJson(object.get("item"));

            ItemInInventoryCondition condition = new ItemInInventoryCondition();
            condition.item = item;

            JsonElement countElement = object.get("count");
            if (countElement != null) {
                condition.count = MinMaxBounds.Ints.fromJson(countElement);
            }

            return condition;
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not load ItemInInventoryCondition ", e);
        }
        return new TrueCondition();
    }
}