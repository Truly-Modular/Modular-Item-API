package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.MaterialCraftingWidget;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.craft.MaterialCraftInfo;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This property manages the allowed Materials for a module
 */
public class AllowedMaterial extends CodecProperty<AllowedMaterial.AllowedMaterialData> implements CraftingProperty, MaterialCraftInfo {
    public static final ResourceLocation KEY = Miapi.id("allowed_material");
    public static AllowedMaterial property;
    public double materialCostClient = 0.0f;
    public double materialRequirementClient = 0.0f;
    public boolean wrongMaterial = false;
    public boolean smithingMaterial = false;
    public int slotHeight = 130 - 14;

    public AllowedMaterial() {
        super(AutoCodec.of(AllowedMaterialData.class).codec());
        property = this;
    }

    public List<String> getAllowedKeys(ItemModule module) {
        Optional<AllowedMaterialData> optional = getData(module);
        if (optional.isPresent()) {
            return optional.get().allowedMaterials;
        }
        return new ArrayList<>();
    }

    public List<Material> getMaterials(String key) {
        return MaterialProperty.materials.values().stream().filter(a -> a.getGroups().contains(key)).collect(Collectors.toList());
    }

    public List<Vec2> getSlotPositions() {
        List<Vec2> test = new ArrayList<>();
        test.add(new Vec2(96, slotHeight - 12));
        return test;
    }

    public boolean shouldExecuteOnCraft(@Nullable ModuleInstance module, ModuleInstance root, ItemStack stack, CraftAction craftAction) {
        JsonElement element = craftAction.data.get(KEY);
        if (element != null) {
            return element.getAsBoolean();
        }
        return CraftingProperty.super.shouldExecuteOnCraft(module, root, stack, craftAction);
    }

    @Override
    public float getPriority() {
        return -1;
    }


    @Environment(EnvType.CLIENT)
    public InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction action) {
        return new MaterialCraftingWidget(this, x, y, width, height, action);
    }

    public Component getWarning() {
        if (wrongMaterial) {
            if (smithingMaterial) {
                Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.material.wrong.smithing");
            }
            return Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.material.wrong");
        }
        return Component.translatable(Miapi.MOD_ID + ".ui.craft.warning.material");
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        Optional<AllowedMaterialData> optional = getData(module);
        ItemStack input = inventory.get(0);
        if (optional.isPresent()) {
            AllowedMaterialData json = optional.get();
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
                smithingMaterial = material.getGroups().contains("smithing");
            } else {
                smithingMaterial = false;
                wrongMaterial = false;
                materialCostClient = 0.0f;
            }
            return false;
        } else {
            wrongMaterial = false;
        }
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        Optional<AllowedMaterialData> optional = getData(module);
        ItemStack input = inventory.get(0);
        ItemStack materialStack = input.copy();
        if (optional.isPresent()) {
            Material material = MaterialProperty.getMaterialFromIngredient(input);
            if (material != null) {
                AllowedMaterialData json = optional.get();
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                if (isAllowed) {
                    MaterialProperty.setMaterial(newModule, material.getID());
                }
                MiapiEvents.MaterialCraftEventData eventData = new MiapiEvents.MaterialCraftEventData(crafting, materialStack, material, newModule, craftAction);
                MiapiEvents.MATERIAL_CRAFT_EVENT.invoker().craft(eventData);
                crafting = eventData.crafted;
                newModule.getRoot().writeToItem(crafting);
            }
        }
        if (crafting.isDamageableItem() && crafting.getDamageValue() > 0) {
            //Miapi.LOGGER.info("dmg " + crafting.getDamage());
            ModuleInstance moduleInstance = craftAction.getModifyingModuleInstance(crafting);
            Double scannedDurability = DurabilityProperty.property.getValue(moduleInstance).orElse(0.0);
            int durability = (int) (scannedDurability.intValue() * MiapiConfig.INSTANCE.server.other.repairRatio);
            //Miapi.LOGGER.info("set dmg to " + (crafting.getDamage() - durability));
            crafting.setDamageValue(crafting.getDamageValue() - durability);
            //Miapi.LOGGER.info("set dmg end " + crafting.getDamage());
        }
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        ModuleInstance newModule = craftAction.getModifyingModuleInstance(crafting);
        //AllowedMaterialJson json = Miapi.gson.decode()
        List<ItemStack> results = new ArrayList<>();
        ItemStack input = inventory.get(0);
        ItemStack materialStack = input.copy();
        Optional<AllowedMaterialData> optional = getData(module);
        if (optional.isEmpty()) {
            Miapi.LOGGER.error("crafting without allowed Material? this probably is a bug!");
            results.add(crafting);
            results.add(input);
            return results;
        }
        AllowedMaterialData json = optional.get();
        Material material = MaterialProperty.getMaterialFromIngredient(input);
        assert material != null;
        int newCount = (int) (input.getCount() - Math.ceil(json.cost * crafting.getCount() / material.getValueOfItem(input)));
        if (!player.level().isClientSide()) {
            input.setCount(newCount);
        }
        assert newModule != null;
        MaterialProperty.setMaterial(newModule, material.getID());

        newModule.getRoot().writeToItem(crafting);
        //materialStack.setCount(1);
        MiapiEvents.MaterialCraftEventData eventData = new MiapiEvents.MaterialCraftEventData(crafting, materialStack, material, newModule, craftAction);
        MiapiEvents.MATERIAL_CRAFT_EVENT.invoker().craft(eventData);
        crafting = eventData.crafted;
        if (crafting.isDamageableItem()) {
            //Miapi.LOGGER.info("dmg " + crafting.getDamage());
            int durability = (int) (DurabilityProperty.property.getValue(craftAction.getModifyingModuleInstance(crafting)).orElse(0.0).intValue() * MiapiConfig.INSTANCE.server.other.repairRatio);
            //Miapi.LOGGER.info("set dmg to " + (crafting.getDamage() - durability));
            crafting.setDamageValue(crafting.getDamageValue() - durability);
            //Miapi.LOGGER.info("set dmg end " + crafting.getDamage());
        }
        results.add(crafting);
        results.add(input);
        return results;
    }

    public static double getMaterialCost(ModuleInstance moduleInstance) {
        Optional<AllowedMaterialData> optional = property.getData(moduleInstance);
        if (optional.isPresent()) {
            return optional.get().cost;
        }
        return 0;
    }

    @Override
    public AllowedMaterialData merge(AllowedMaterialData left, AllowedMaterialData right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    @Override
    public int getSlotHeight() {
        return slotHeight;
    }

    @Override
    public void setSlotHeight(int newHeight) {
        slotHeight = newHeight;
    }

    @Override
    public double getMaterialCostClient() {
        return materialCostClient;
    }

    @Override
    public double getMaterialRequirementClient() {
        return materialRequirementClient;
    }

    public static class AllowedMaterialData {
        public List<String> allowedMaterials;
        @CodecBehavior.Optional
        public float cost = 1;
    }
}
