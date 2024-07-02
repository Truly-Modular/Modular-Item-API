package smartin.miapi.blueprint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
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
        PropertyResolver.register("blueprint", (moduleInstance, oldMap) -> {
            if (moduleInstance != null && moduleInstance.moduleData.containsKey("blueprint")) {
                Blueprint blueprint = Blueprint.blueprintRegistry.get(moduleInstance.moduleData.get("blueprint"));
                if (blueprint != null && !blueprint.writeToItem) {
                    return ModuleProperty.mergeList(oldMap, blueprint.upgrades, MergeType.SMART);
                }
            }
            return oldMap;
        });
        ReloadEvents.END.subscribe((isClient -> {
            Blueprint.blueprintRegistry.clear();
            RegistryInventory.modules.getFlatMap().forEach((s, module) -> {
                Blueprint blueprint = new Blueprint();
                blueprint.isAllowed = ConditionManager.get(null);
                blueprint.module = module;
                blueprint.key = "testing" + s;
                //Blueprint.blueprintRegistry.register(blueprint.key, blueprint);
            });
        }));
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        ReplaceView.optionSuppliers.add(option -> {
            List<CraftOption> options = new ArrayList<>();
            Blueprint.blueprintRegistry.getFlatMap().forEach((key, blueprint) -> {
                BlockPos pos;
                Map<ModuleProperty, JsonElement> propertyMap = new HashMap<>();
                if (option.getInstance() != null) {
                    propertyMap = option.getInstance().getOldProperties();
                }
                if (option.getWorkbench() != null) {
                    pos = option.getWorkbench().getBlockPos();
                } else {
                    pos = null;
                }
                if (
                        blueprint.isAllowed.isAllowed(new ConditionManager.ModuleConditionContext(option.getInstance(), pos, option.getPlayer(), propertyMap, new ArrayList<>()))) {
                    if (
                            option.getSlot().allowedIn(new ModuleInstance(blueprint.module))) {
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        String blueprintKey = data.get("blueprint");
        if (blueprintKey != null && newModule != null) {
            newModule.moduleData.put("blueprint", blueprintKey);
            Blueprint blueprint = Blueprint.blueprintRegistry.get(blueprintKey);
            if (blueprint != null && blueprint.writeToItem) {
                writePropertiesToModule(newModule, blueprint.upgrades);
            }
            newModule.getRoot().writeToItem(old);
        }
        return old;
    }

    public static void writePropertiesToModule(ModuleInstance moduleInstance, Map<ModuleProperty, JsonElement> properties) {
        String rawProperties = moduleInstance.moduleData.get("properties");
        if (rawProperties != null) {
            Map<ModuleProperty, JsonElement> nextMap = new HashMap<>(properties);
            JsonObject moduleJson = Miapi.gson.fromJson(rawProperties, JsonObject.class);
            if (moduleJson != null) {
                moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                    if (property != null) {
                        nextMap.put(property, stringJsonElementEntry.getValue());
                    }
                });
            }
            moduleInstance.moduleData.put("properties", Miapi.gson.toJson(nextMap));
        }
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
