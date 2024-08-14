package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.conditions.TrueCondition;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This property can manage if a module can be crafted in the first place
 */
public class CraftingConditionProperty extends CodecProperty<CraftingConditionProperty.CraftingConditionJson> implements CraftingProperty {
    public static final String KEY = "crafting_condition";
    public static CraftingConditionProperty property;
    public static PrioritizedEvent<CanCraft> CAN_CRAFT_SELECT_EVENT = PrioritizedEvent.createEventResult();
    public static Codec<CraftingConditionJson> CODEC = AutoCodec.of(CraftingConditionJson.class).codec();

    public CraftingConditionProperty() {
        super(CODEC);
        property = this;
    }

    public static boolean isVisible(SlotProperty.ModuleSlot slot, ItemModule module, Player entity, BlockPos pos) {
        CraftingConditionJson json = (CraftingConditionJson) module.properties().get(property);
        if (json != null) {
            return json.visible.isAllowed(ConditionManager.fullContext(new ModuleInstance(module), pos, entity, module.properties()));
        }
        return true;
    }

    public static boolean isSelectAble(SlotProperty.ModuleSlot slot, @Nullable ItemModule module, Player entity, BlockPos pos) {
        CraftingConditionJson json = module != null ? (CraftingConditionJson) module.properties().get(property) : null;
        if (module == null) {
            module = ItemModule.empty;
        }
        ConditionManager.ConditionContext context = ConditionManager.fullContext(new ModuleInstance(module), pos, entity, module.properties());
        if (json != null && !json.selectAble.isAllowed(context.copy())) {
            return false;
        }
        return !CAN_CRAFT_SELECT_EVENT.invoker().craft(slot, module, context).interruptsFurtherEvaluation();
    }

    public static List<Component> getReasonsForSelectable(SlotProperty.ModuleSlot slot, ItemModule module, Player entity, BlockPos pos) {
        CraftingConditionJson json = module != null ? (CraftingConditionJson) module.properties().get(property) : null;
        if (module == null) {
            module = ItemModule.empty;
        }
        List<Component> reasons = new ArrayList<>();
        ConditionManager.ConditionContext context = ConditionManager.fullContext(new ModuleInstance(module), pos, entity, module.properties());
        ConditionManager.ConditionContext secondContext = context.copy();
        if (json != null) {
            json.selectAble.isAllowed(context);
            reasons.addAll(context.failReasons);
        }
        if (CAN_CRAFT_SELECT_EVENT.invoker().craft(slot, module, secondContext).interruptsFurtherEvaluation()) {
            reasons.addAll(secondContext.failReasons);
        }
        return reasons;
    }

    @Override
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        CraftingConditionJson json = module != null ? (CraftingConditionJson) module.properties().get(property) : null;
        if (module == null) {
            module = ItemModule.empty;
        }
        BlockPos pos = new BlockPos(0, 0, 0);
        if (bench != null) {
            pos = bench.getBlockPos();
        } else {
            Miapi.LOGGER.error("bench is null. this should never happen");
        }
        ConditionManager.ConditionContext context = ConditionManager.fullContext(new ModuleInstance(module), pos, player, module.properties());
        return json == null || !json.craftAble.isAllowed(context);
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        return crafting;
    }

    @Override
    public CraftingConditionJson merge(CraftingConditionJson left, CraftingConditionJson right, MergeType mergeType) {
        if (MergeType.EXTEND.equals(mergeType)) {
            return left;
        }
        return right;
    }

    public interface CanCraft {
        EventResult craft(SlotProperty.ModuleSlot slot, ItemModule module, ConditionManager.ConditionContext context);
    }

    public static class CraftingConditionJson {
        @CodecBehavior.Optional
        public ModuleCondition visible = new TrueCondition();
        @CodecBehavior.Optional
        public ModuleCondition selectAble = new TrueCondition();
        @CodecBehavior.Optional
        public ModuleCondition craftAble = new TrueCondition();
    }
}
