package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.replace.MaterialCraftingWidget;
import smartin.miapi.craft.BlueprintComponent;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.craft.MaterialCraftInfo;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.ServerReplaceProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;

import java.util.List;
import java.util.Map;

/**
 * this isnt really an property. its only a property since properties is given a lot of access into the crafting internals
 */
public class BlueprintCrafting extends ServerReplaceProperty implements CraftingProperty {
    public static ResourceLocation KEY = Miapi.id("runtime_blueprint_property");
    double materialCostClient = 0;
    double materialRequirementClient = 0;
    int slotHeight = 19;


    @Environment(EnvType.CLIENT)
    public InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction craftAction) {
        BlueprintComponent blueprintComponent = BlueprintComponent.getBlueprint(craftAction.data, craftAction.screenHandler);
        if (blueprintComponent != null && !blueprintComponent.useMaterialCrafting()) {
            return new MaterialCraftingWidget(new MaterialCraftInfo() {

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

                @Override
                public boolean renderMaterialWidget() {
                    return false;
                }
            }, x, y, width, height, craftAction);
        }
        return null;
    }

    public List<Vec2> getSlotPositions() {
        return List.of(new Vec2(96, slotHeight - 12));
    }

    @Override
    public boolean shouldExecuteOnCraft(@Nullable ModuleInstance module, ModuleInstance root, ItemStack stack, CraftAction craftAction) {
        BlueprintComponent blueprintComponent = BlueprintComponent.getBlueprint(craftAction.data, craftAction.screenHandler);
        return blueprintComponent != null;
    }

    /**
     * setting priority low to execute before {@link AllowedMaterial} so {@link AllowedMaterial} can set the material
     */
    public float getPriority() {
        return -10;
    }

    public boolean canPerform(ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench, Player player, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        BlueprintComponent blueprintComponent = BlueprintComponent.getBlueprint(craftAction.data, craftAction.screenHandler);
        if (blueprintComponent != null) {
            if (blueprintComponent.useMaterialCrafting()) {
                return true;
            } else {
                ItemStack input = inventory.getFirst();
                this.materialRequirementClient = blueprintComponent.getCost();
                this.materialCostClient = 0;
                if (blueprintComponent.isValid(input, blueprintComponent.retrieve(craftAction.screenHandler))) {
                    this.materialCostClient = input.getCount();
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        BlueprintComponent blueprintComponent = BlueprintComponent.getBlueprint(craftAction.data, craftAction.screenHandler);
        if (blueprintComponent != null) {
            ModuleInstance moduleInstance = craftAction.getModifyingModuleInstance(crafting);
            if (moduleInstance != null) {
                blueprintComponent.apply(moduleInstance);
                moduleInstance.getRoot().writeToItem(crafting);
            }
        }
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, Player player, @Nullable ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        List<ItemStack> result = CraftingProperty.super.performCraftAction(old, crafting, player, bench, craftAction, module, inventory, data);
        BlueprintComponent blueprintComponent = BlueprintComponent.getBlueprint(craftAction.data, craftAction.screenHandler);
        if (blueprintComponent != null && !blueprintComponent.useMaterialCrafting()) {
            result.set(1, blueprintComponent.adjustCost(inventory.getFirst()));
        }
        return result;
    }
}
