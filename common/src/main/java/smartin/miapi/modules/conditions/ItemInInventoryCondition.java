package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redpxnda.nucleus.codec.MiscCodecs;
import com.redpxnda.nucleus.util.MiscUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class ItemInInventoryCondition implements ModuleCondition {
    public Item item;
    public NumberRange.IntRange count = NumberRange.IntRange.atLeast(1);

    public ItemInInventoryCondition() {
    }

    @Override
    public boolean isAllowed(@Nullable ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, @Nullable Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        if (player != null && count.test(player.getInventory().count(item))) return true;

        Text text;

        int min = count.getMin() == null ? 0 : count.getMin();
        Integer max = count.getMax();

        if (max != null) text = Text.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.specific", min, max, Registries.ITEM.getId(item).toString());
        else text = Text.translatable(Miapi.MOD_ID + ".condition.item_in_inventory.error.no_max", min, Registries.ITEM.getId(item).toString());

        reasons.add(text);
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        if (element instanceof JsonPrimitive primitive) {
            Item item = MiscCodecs.quickParse(primitive, Registries.ITEM.getCodec(), s -> Miapi.LOGGER.error("Error parsing item for ItemInInventoryCondition -> " + s));
            return MiscUtil.initialize(new ItemInInventoryCondition(), c -> c.item = item);
        } else if (element instanceof JsonObject object) {
            if (!object.has("item")) throw new RuntimeException("Expected key 'item' for ItemInInventoryCondition, but it was not found.");
            Item item = MiscCodecs.quickParse(object.get("item"), Registries.ITEM.getCodec(), s -> Miapi.LOGGER.error("Error parsing item for ItemInInventoryCondition -> " + s));

            ItemInInventoryCondition condition = new ItemInInventoryCondition();
            condition.item = item;

            JsonElement countElement = object.get("count");
            if (countElement != null) {
                condition.count = NumberRange.IntRange.fromJson(countElement);
            }

            return condition;
        } else {
            throw new RuntimeException("Expected either a JsonPrimitive(String) or JsonObject for ItemInInventoryCondition, but instead received a " + element.getClass() + ": " + element);
        }
    }
}