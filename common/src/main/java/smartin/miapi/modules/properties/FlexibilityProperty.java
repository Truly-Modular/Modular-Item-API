package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
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
    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        Double flexibility = getValue(crafting);
        flexibility = flexibility == null ? 0 : flexibility;
        return flexibility >= 0;
    }

    @Override
    public boolean canPerformOnRemove(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        Double flexibility = getValue(crafting);
        flexibility = flexibility == null ? 0 : flexibility;
        return flexibility >= 0;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return crafting;
    }
}
