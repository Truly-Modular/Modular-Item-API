package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NBTWriteProperty implements ModuleProperty, CraftingProperty {
    public static NBTWriteProperty property;
    public static String KEY = "nbt";

    public NBTWriteProperty() {
        property = this;
    }


    public Map<String, NbtElement> customNBT(ItemStack itemStack) {
        Map<String, NbtElement> map = new HashMap<>();
        JsonElement data = ItemModule.getMergedProperty(itemStack, property);
        if (data != null) {
            data.getAsJsonObject().asMap().forEach((key, json) -> map.put(key, JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json)));
        }
        return map;
    }

    public boolean shouldExecuteOnCraft(@Nullable ItemModule.ModuleInstance module, ItemModule.ModuleInstance root, ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        Map<String, NbtElement> oldData = customNBT(old);
        Map<String, NbtElement> newData = customNBT(old);
        for (String key : oldData.keySet()) {
            crafting.removeSubNbt(key);
        }
        for (String key : newData.keySet()) {
            crafting.setSubNbt(key, oldData.get(key));
        }

        return crafting;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonObject();
        return true;
    }
}
