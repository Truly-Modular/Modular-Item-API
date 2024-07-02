package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class StatRequirementProperty implements ModuleProperty, CraftingProperty {
    public static final String KEY = "statRequirements";
    public static StatRequirementProperty property;

    public StatRequirementProperty() {
        property = this;
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        if (bench == null) return true;

        JsonElement element = newModule.getOldProperties().get(property);
        if (element != null){
            AtomicBoolean canCraft = new AtomicBoolean(true);

            element.getAsJsonObject().asMap().forEach((key, val) -> {
                CraftingStat<?> stat = RegistryInventory.craftingStats.get(key);
                if (stat != null) {
                    Object instance = bench.getStat(stat);
                    boolean craftable = ((CraftingStat) stat).canCraft(instance == null ? stat.getDefault() : instance, stat.createFromJson(val, newModule), old, crafting, bench, player, newModule, module, inventory, data);
                    if (!craftable) canCraft.set(false);
                }
            });

            return canCraft.get();
        }

        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String,String> data) {
        return crafting;
    }

    @Override
    public Component getWarning() {
        return Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.crafting_stat");
    }

    @Override
    public float getPriority() {
        return 0f;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        assert data instanceof JsonObject;
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge;
            }
            case SMART, EXTEND -> {
                JsonObject obj = old.deepCopy().getAsJsonObject();
                toMerge.getAsJsonObject().asMap().forEach(obj::add);
                return obj;
            }
        }
        return old;
    }
}
