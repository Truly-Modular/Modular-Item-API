package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This property can manage if a module can be crafted in the first place
 */
public class CraftingConditionProperty implements ModuleProperty, CraftingProperty {
    public static final String KEY = "crafting_condition";
    public static CraftingConditionProperty property;
    public static PrioritizedEvent<CanCraft> CRAFT_CONDITION_EVENT = PrioritizedEvent.createEventResult();

    public CraftingConditionProperty() {
        property = this;
    }

    public static boolean isVisible(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        if (element != null) {
            return new CraftingConditionJson(element).getVisible().isAllowed(new ConditionManager.ModuleConditionContext(slot.parent, pos, entity, module.getKeyedProperties(), reasons));
        }
        return true;
    }

    public static boolean isCraftable(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        ModuleInstance instance = slot == null ? null : slot.parent;
        Map<ModuleProperty, JsonElement> elementMap = module.getKeyedProperties();
        if (instance != null) {
            elementMap = instance.getProperties();
        }
        if (element != null) {
            CraftingConditionJson conditionJson = new CraftingConditionJson(element);
            if (!conditionJson.getCraftAble().isAllowed(new ConditionManager.ModuleConditionContext(instance, pos, entity, elementMap, reasons))) {
                return false;
            }
        }
        if (CRAFT_CONDITION_EVENT.invoker().craft(slot, module, new ConditionManager.ModuleConditionContext(instance, pos, entity, elementMap, reasons)).interruptsFurtherEvaluation()) {
            return false;
        }
        return true;
    }

    public static void inSlotPlaced(SlotProperty.ModuleSlot slot, ItemModule module, Consumer<ModuleInstance> test) {
        ModuleInstance moduleInstance = new ModuleInstance(module);
        if (slot != null && slot.parent != null) {
            slot.inSlot = moduleInstance;
            moduleInstance.parent = slot.parent;
        }
        test.accept(moduleInstance);
        moduleInstance.parent = null;
        if (slot != null && slot.parent != null) {
            slot.inSlot = null;
        }
    }

    public static List<Text> getReasonsForCraftable(SlotProperty.ModuleSlot slot, ItemModule module, PlayerEntity entity, BlockPos pos) {
        JsonElement element = module.getKeyedProperties().get(property);
        List<Text> reasons = new ArrayList<>();
        ModuleInstance instance = slot == null ? null : slot.parent;
        Map<ModuleProperty, JsonElement> elementMap = module.getKeyedProperties();
        if (instance != null) {
            elementMap = instance.getProperties();
        }
        if (element != null) {
            new CraftingConditionJson(element).getCraftAble().isAllowed(new ConditionManager.ModuleConditionContext(instance, pos, entity, elementMap, reasons));
        }
        CRAFT_CONDITION_EVENT.invoker().craft(slot, module, new ConditionManager.ModuleConditionContext(instance, pos, entity, elementMap, reasons));
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
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public Text getWarning() {
        return CraftingProperty.super.getWarning();
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, PlayerEntity player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        if (newModule != null) {
            JsonElement element = newModule.getProperties().get(property);
            if (element != null) {
                List<Text> reasons = new ArrayList<>();
                return new CraftingConditionJson(element).getOnCraftAble().isAllowed(new ConditionManager.ModuleConditionContext(newModule, null, player, newModule.getProperties(), reasons));
            }
        }
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        return crafting;
    }

    public interface CanCraft {
        EventResult craft(SlotProperty.ModuleSlot slot, ItemModule module, ConditionManager.ModuleConditionContext context);
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

        public ModuleCondition getOnCraftAble() {
            return onCraft;
        }
    }
}
