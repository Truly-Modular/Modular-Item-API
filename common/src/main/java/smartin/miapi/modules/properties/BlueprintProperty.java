package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.blueprint.Blueprint;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintProperty implements CraftingProperty, ModuleProperty {
    public static String KEY = "blueprint";
    public static BlueprintProperty property;

    public BlueprintProperty() {
        property = this;
        if (Environment.isClient()) {
            setupClient();
        }
        ReloadEvents.END.subscribe((isClient -> {
            Blueprint.blueprintRegistry.clear();
            RegistryInventory.modules.getFlatMap().forEach((s, module) -> {
                Blueprint blueprint = new Blueprint();
                blueprint.isAllowed = ConditionManager.get(null);
                blueprint.module = module;
                blueprint.key = "testing" + s;
                Blueprint.blueprintRegistry.register(blueprint.key, blueprint);
            });
        }));
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        ReplaceView.optionSuppliers.add(option -> {
            List<CraftOption> options = new ArrayList<>();
            Blueprint.blueprintRegistry.getFlatMap().forEach((key, blueprint) -> {
                BlockPos pos;
                Map<ModuleProperty,JsonElement> propertyMap = new HashMap<>();
                if(option.getInstance()!=null){
                    propertyMap = option.getInstance().getProperties();
                }
                if (option.getWorkbench() != null) {
                    pos = option.getWorkbench().getPos();
                } else {
                    pos = null;
                }
                if (
                        blueprint.isAllowed.isAllowed(option.getInstance(), pos, option.getPlayer(), propertyMap, new ArrayList<>())) {
                    if(option.getSlot().allowedIn(new ItemModule.ModuleInstance(blueprint.module))){
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("blueprint", blueprint.key);
                        options.add(new CraftOption(blueprint.module, dataMap));
                    }
                }
            });
            return options;
        });
    }

    @Override
    public void writeData(Map<String, String> data, @Nullable InteractAbleWidget createdGui, EditOption.EditContext editContext) {
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.@Nullable ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        String blueprintKey = data.get("blueprint");
        if (blueprintKey != null && newModule != null) {
            newModule.moduleData.put("blueprint", blueprintKey);
            newModule.getRoot().writeToItem(old);
        }
        return old;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
