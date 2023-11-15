package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This property can manage if a module can be crafted in the first place
 */
public class CraftingConditionProperty implements ModuleProperty, CraftingProperty {
    public static final String KEY = "crafting_condition";
    public static CraftingConditionProperty property;

    public CraftingConditionProperty() {
        property = this;
    }

    public static boolean isVisible(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        if (element != null) {
            return new CraftingConditionJson(element).getVisible().isAllowed(slot.parent, pos, entity, module.getKeyedProperties(), reasons);
        }
        return true;
    }

    public static boolean isCraftable(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        if (element != null) {
            ItemModule.ModuleInstance instance = slot == null ? null : slot.parent;
            CraftingConditionJson conditionJson = new CraftingConditionJson(element);
            return conditionJson.getCraftAble().isAllowed(instance, pos, entity, module.getKeyedProperties(), reasons);
        }
        return true;
    }

    public static List<Text> getReasonsForCraftable(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        if (element != null) {
            ItemModule.ModuleInstance instance = slot == null ? null : slot.parent;
            new CraftingConditionJson(element).getCraftAble().isAllowed(instance, pos, entity, module.getKeyedProperties(), reasons);
        }
        return reasons;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.super.merge(old, toMerge, type);
    }

    @Override
    public boolean shouldExecuteOnCraft(ItemModule.ModuleInstance module, ItemModule.ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public Text getWarning() {
        return CraftingProperty.super.getWarning();
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, PlayerEntity player, @Nullable ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        if(newModule != null){
            JsonElement element = newModule.getProperties().get(property);
            if(element!=null){
                List<Text> reasons = new ArrayList<>();
                return new CraftingConditionJson(element).craftAble.isAllowed(newModule,null,player,newModule.getProperties(),reasons);
            }
        }
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        return crafting;
    }

    public static class CraftingConditionJson {
        public ModuleCondition visible;
        public ModuleCondition craftAble;
        public ModuleCondition onCraft;

        public CraftingConditionJson(JsonElement element) {
            visible = ConditionManager.get(element.getAsJsonObject().get("visible"));
            craftAble = ConditionManager.get(element.getAsJsonObject().get("craftable"));
            onCraft = ConditionManager.get(element.getAsJsonObject().get("on_craft"));
        }

        public ModuleCondition getVisible() {
            return visible;
        }

        public ModuleCondition getCraftAble() {
            return craftAble;
        }
    }
}
