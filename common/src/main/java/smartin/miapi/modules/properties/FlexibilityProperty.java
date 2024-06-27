package smartin.miapi.modules.properties;

import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Flexibility is a optional tool to limit items to be somewhat reasonable
 */
public class FlexibilityProperty extends DoubleProperty implements CraftingProperty {
    public static final String KEY = "flexibility";
    public static DoubleProperty property;

    public FlexibilityProperty() {
        super(KEY);
        property = this;
    }

    public Component getWarning() {
        return Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.flexibility");
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }

    @Override
    public boolean shouldExecuteOnCraft(ModuleInstance module, ModuleInstance root, ItemStack stack) {
        return ItemModule.getMergedProperty(root,this)!=null;
    }


    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        double flexibility = getValueSafe(crafting);
        return flexibility >= 0;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        return crafting;
    }
}
