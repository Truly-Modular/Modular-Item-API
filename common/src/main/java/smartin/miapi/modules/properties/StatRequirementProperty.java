package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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


//TODO: @Panda rework this and Stats in general
public class StatRequirementProperty implements ModuleProperty<Object>, CraftingProperty {
    public static final ResourceLocation KEY = Miapi.id("stat_requirements");
    public static StatRequirementProperty property;

    public StatRequirementProperty() {
        property = this;
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        if (bench == null) return true;

        JsonElement element = null;
        if (element != null) {
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
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
    public Object decode(JsonElement element) {
        return null;
    }

    @Override
    public Object merge(Object left, Object right, MergeType mergeType) {
        return null;
    }

    @Override
    public JsonElement encode(Object property) {
        return null;
    }
}
