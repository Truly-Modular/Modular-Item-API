package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

import java.util.List;
import java.util.Objects;

public class FlexibilityProperty extends SimpleDoubleProperty implements CraftingProperty {
    public static final String KEY = "flexibility";
    public static SimpleDoubleProperty property;

    public FlexibilityProperty() {
        super(KEY);
        property = this;
    }

    @Override
    public float getPriority() {
        return 0;
    }

    public Text getWarning() {
        return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.flexibility");
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
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (Objects.requireNonNull(type) == MergeType.SMART || type == MergeType.EXTEND) {
            return Miapi.gson.toJsonTree(toMerge.getAsDouble() + old.getAsDouble());
        } else if (type == MergeType.OVERWRITE) {
            return toMerge;
        }
        return old;
    }

    @Override
    public boolean shouldExecuteOnCraft(ItemModule.ModuleInstance module) {
        return ItemModule.getMergedProperty(module.getRoot(),this)!=null;
    }


    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        double flexibility = getValueSafe(crafting);
        return flexibility >= 0;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return crafting;
    }
}
