package smartin.miapi.modules.properties.inventory;

import com.google.gson.JsonElement;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.List;

public class InventoryProperty extends DoubleProperty {
    public static InventoryProperty property;

    public InventoryProperty(ResourceLocation cacheKey) {
        super(cacheKey);
        property = this;
    }

    public List<ItemStack> getSlots(ModuleInstance moduleInstance, ItemStack backPackItem) {
        int slotCount = this.getValue(moduleInstance).get().intValue();
        NonNullList<ItemStack> slots = NonNullList.withSize(slotCount, ItemStack.EMPTY);

        // Load stored data from the module instance
        JsonElement data = moduleInstance.moduleData.get(id);
        if (data != null && data.isJsonArray()) {
            Tag tag = JsonOpsBooleanPatched.INSTANCE.convertTo(Miapi.BOOL_CORRECTED_OPS, data);
            if (tag instanceof CompoundTag compoundTag) {
                ContainerHelper.loadAllItems(compoundTag, slots, moduleInstance.registryAccess);
            }
        }
        return slots;
    }

    public void save(ModuleInstance moduleInstance, List<ItemStack> items) {
        int slotCount = this.getValue(moduleInstance).get().intValue();
        NonNullList<ItemStack> slots = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(slotCount, items.size()); i++) {
            slots.set(i, items.get(i));
        }
        ContainerHelper.saveAllItems(new CompoundTag(), slots, moduleInstance.registryAccess);
    }
}
