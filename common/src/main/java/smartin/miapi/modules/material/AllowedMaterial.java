package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.MaterialCraftingWidget;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This property manages the allowed Materials for a module
 */
public class AllowedMaterial implements CraftingProperty, ModuleProperty {
    public static final String KEY = "allowedMaterial";
    public static AllowedMaterial property;
    public double materialCostClient = 0.0f;
    public double materialRequirementClient = 0.0f;
    public boolean wrongMaterial = false;
    public int slotHeight = 130 - 14;

    public AllowedMaterial() {
        property = this;
    }

    public List<String> getAllowedKeys(ItemModule module) {
        JsonElement element = module.getProperties().get(KEY);
        if (element != null) {
            AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
            return json.allowedMaterials;
        } else {
            return new ArrayList<>();
        }
    }

    public List<Material> getMaterials(String key) {
        return MaterialProperty.materials.values().stream().filter(a -> a.getGroups().contains(key)).collect(Collectors.toList());
    }

    public List<Vec2f> getSlotPositions() {
        List<Vec2f> test = new ArrayList<>();
        test.add(new Vec2f(96, slotHeight - 12));
        return test;
    }

    @Override
    public float getPriority() {
        return -1;
    }


    @Environment(EnvType.CLIENT)
    public InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction action) {
        return new MaterialCraftingWidget(this, x, y, width, height, action);
    }

    public Text getWarning() {
        if (wrongMaterial) {
            return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.material.wrong");
        }
        return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.material");
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, PlayerEntity player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        if (element != null) {
            AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
            Material material = MaterialProperty.getMaterialFromIngredient(input);
            materialRequirementClient = json.cost * crafting.getCount();
            if (material != null) {
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                wrongMaterial = !isAllowed;
                if (isAllowed) {
                    materialCostClient = input.getCount() * material.getValueOfItem(input);
                    return materialCostClient >= materialRequirementClient;
                } else {
                    materialCostClient = 0.0f;
                }
            } else {
                wrongMaterial = false;
                materialCostClient = 0.0f;
            }
        } else {
            wrongMaterial = false;
        }
        return false;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ItemModule.ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        ItemStack materialStack = input.copy();
        if (element != null) {
            Material material = MaterialProperty.getMaterialFromIngredient(input);
            if (material != null) {
                AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                if (isAllowed) {
                    MaterialProperty.setMaterial(newModule, material.getKey());
                }
                MiapiEvents.MaterialCraftEventData eventData = new MiapiEvents.MaterialCraftEventData(crafting, materialStack, material, newModule, craftAction);
                MiapiEvents.MATERIAL_CRAFT_EVENT.invoker().craft(eventData);
                crafting = eventData.crafted;
            }
        }
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ItemModule.ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        List<ItemStack> results = new ArrayList<>();
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        ItemStack materialStack = input.copy();
        AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
        Material material = MaterialProperty.getMaterialFromIngredient(input);
        assert material != null;
        int newCount = (int) (input.getCount() - Math.ceil(json.cost * crafting.getCount() / material.getValueOfItem(input)));
        if (!player.getWorld().isClient()) {
            input.setCount(newCount);
        }
        assert newModule != null;
        MaterialProperty.setMaterial(newModule, material.getKey());
        //materialStack.setCount(1);
        MiapiEvents.MaterialCraftEventData eventData = new MiapiEvents.MaterialCraftEventData(crafting, materialStack, material, newModule, craftAction);
        MiapiEvents.MATERIAL_CRAFT_EVENT.invoker().craft(eventData);
        crafting = eventData.crafted;
        results.add(crafting);
        results.add(input);
        return results;
    }

    public static double getMaterialCost(ItemModule.ModuleInstance moduleInstance) {
        JsonElement element = property.getJsonElement(moduleInstance);
        if (element != null) {
            return Miapi.gson.fromJson(element, AllowedMaterialJson.class).cost;
        }
        return 0;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    static class AllowedMaterialJson {
        public List<String> allowedMaterials;
        public float cost;
    }
}
