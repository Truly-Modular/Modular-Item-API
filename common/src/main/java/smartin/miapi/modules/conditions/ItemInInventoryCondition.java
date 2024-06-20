package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.NumberRange;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;

import java.util.List;

public class ItemInInventoryCondition implements ModuleCondition {
    public Ingredient item;
    public NumberRange.IntRange count = NumberRange.IntRange.atLeast(1);

    public ItemInInventoryCondition() {
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            PlayerEntity player = moduleConditionContext.player;
            List<Text> reasons = moduleConditionContext.reasons;
            if (player != null && count.test(getCount(player.getInventory(), item))) return true;

            Text text;

            int min = count.getMin() == null ? 0 : count.getMin();
            Integer max = count.getMax();
            String ingredientName = "";
            if(item.getMatchingStacks()!=null && item.getMatchingStacks().length>1){
                ingredientName = Text.translatable(item.getMatchingStacks()[0].getItem().getTranslationKey()).toString();
            }

            if (max != null)
                text = Text.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.specific", min, max, ingredientName);
            else
                text = Text.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.no_max", min, ingredientName);

            reasons.add(text);
        }
        return false;
    }

    public int getCount(Inventory inventory, Ingredient ingredient) {
        int found = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (ingredient.test(inventory.getStack(i))) {
                found += inventory.getStack(i).getCount();
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
                condition.count = NumberRange.IntRange.fromJson(countElement);
            }

            return condition;
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not load ItemInInventoryCondition ", e);
        }
        return new TrueCondition();
    }
}