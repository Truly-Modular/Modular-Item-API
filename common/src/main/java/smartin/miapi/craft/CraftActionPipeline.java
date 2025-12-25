package smartin.miapi.craft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

public class CraftActionPipeline {

    public enum FailureMode {
        STRICT,
        NORMAL,
        LENIENT
    }

    private final Player player;
    private ItemStack itemBase = null;
    private int itemCount = 1;
    private FailureMode failureMode = FailureMode.NORMAL;
    private boolean allowEmptyModules = false;
    private boolean autoCount = true;

    private final List<PipelineEntry> entries = new ArrayList<>();

    private CraftActionPipeline(Player player) {
        this.player = player;
    }

    public static CraftActionPipeline create(Player player) {
        return new CraftActionPipeline(player);
    }

    /* -----------------------------------------------------------
     * Configuration
     * -----------------------------------------------------------
     */

    /**
     * Sets optional base item.
     * Use if modifying an existing item - otherwise leave empty
     *
     * @param stack
     * @return
     */
    public CraftActionPipeline baseItem(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            this.itemBase = stack.copy();
        } else {
            this.baseItem(null);
        }
        return this;
    }

    public CraftActionPipeline itemCount(int count) {
        this.itemCount = Math.max(1, Math.min(64, count));
        return this;
    }

    public CraftActionPipeline allowEmptyModules(boolean allowed) {
        this.allowEmptyModules = allowed;
        return this;
    }

    public CraftActionPipeline failureMode(FailureMode mode) {
        this.failureMode = mode;
        return this;
    }

    public CraftActionPipeline setAutoCount(boolean autoCount) {
        this.autoCount = autoCount;
        return this;
    }

    /* -----------------------------------------------------------
     * Add entries
     * -----------------------------------------------------------
     */

    public CraftActionPipeline add(
            List<String> moduleSlot,
            ResourceLocation moduleId,
            int containerIndex,
            Container container
    ) {
        Objects.requireNonNull(moduleId, "moduleId");
        Objects.requireNonNull(container, "container");

        ItemModule module = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(moduleId);
        if (module == null) {
            throw new IllegalArgumentException("Unknown module: " + moduleId);
        }

        this.entries.add(new PipelineEntry(moduleSlot, module, containerIndex, container));
        return this;
    }

    public CraftActionPipeline add(
            String moduleSlot, ResourceLocation moduleId,
            int containerIndex, Container container
    ) {
        return add(List.of(moduleSlot), moduleId, containerIndex, container);
    }

    public CraftActionPipeline add(
            ResourceLocation moduleId,
            int containerIndex, Container container
    ) {
        return add(List.of(), moduleId, containerIndex, container);
    }

    /* -----------------------------------------------------------
     * Core Operations
     * -----------------------------------------------------------
     */

    public boolean validate() {
        ItemStack working = getBaseStack();

        for (PipelineEntry e : entries) {
            ItemStack input = e.container.getItem(e.slotIndex);

            if (input.isEmpty() && !allowEmptyModules) {
                if (isNormalOrStrict()) return false;
                continue;
            }
            if (input.isEmpty()) continue;

            CraftAction action = createAction(working, e);
            action.linkInventory(e.container, e.slotIndex);

            if (!action.canPerform() && failureMode == FailureMode.NORMAL) {
                return false;
            }
        }
        return true;
    }

    public ItemStack preview() {
        ItemStack working = getBaseStack();

        for (PipelineEntry e : entries) {
            ItemStack input = e.container.getItem(e.slotIndex);

            if (input.isEmpty() && !allowEmptyModules) {
                if (isNormalOrStrict()) return ItemStack.EMPTY;
                continue;
            }
            if (input.isEmpty()) continue;

            CraftAction action = createAction(working, e);
            action.linkInventory(e.container, e.slotIndex);

            if (!action.canPerform()) {
                if (isNormalOrStrict()) return ItemStack.EMPTY;
                continue;
            }

            working = action.getPreview();
            if (working.isEmpty() && isNormalOrStrict()) {
                return ItemStack.EMPTY;
            }
        }
        return working;
    }

    public int calculateMaxProduction() {
        List<Integer> results = new ArrayList<>();

        for (PipelineEntry e : entries) {
            results.add(calculateAvailableForEntry(e));
        }

        // Final output is governed by minimum limiting factor
        int min = results.stream().min(Integer::compareTo).orElse(0);
        if (min == 0 && FailureMode.STRICT == failureMode) {
            return 0;
        }
        if (!autoCount) {
            return itemCount;
        }
        return Math.clamp(min, 1, 64);
    }

    private int calculateAvailableForEntry(PipelineEntry entry) {
        ItemStack stack = entry.container.getItem(entry.slotIndex);
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        // Resolve material
        Material material = MaterialProperty.getMaterialFromIngredient(stack);
        if (material == null) {
            return 0;
        }

        double itemValue = material.getValueOfItem(stack);
        if (itemValue <= 0) {
            return 0;
        }

        // Resolve allowed material property for the module
        Optional<AllowedMaterial.AllowedMaterialData> allowedData =
                AllowedMaterial.property.getData(entry.module);

        if (allowedData.isEmpty()) {
            return 0;
        }

        Float cost = allowedData.get().cost;
        if (cost == null || cost <= 0) {
            return 0;
        }

        return (int) Math.floor(itemValue / cost * stack.getCount());
    }


    private boolean isNormalOrStrict() {
        return failureMode == FailureMode.NORMAL || failureMode == FailureMode.STRICT;
    }

    public ItemStack perform() {
        ItemStack working = getBaseStack();

        for (PipelineEntry e : entries) {
            ItemStack input = e.container.getItem(e.slotIndex);

            if (input.isEmpty() && !allowEmptyModules) {
                if (isNormalOrStrict()) return working;
                continue;
            }
            if (input.isEmpty()) continue;

            CraftAction action = createAction(working, e);
            action.linkInventory(e.container, e.slotIndex);

            if (!action.canPerform()) {
                if (isNormalOrStrict()) return working;
                continue;
            }

            working = action.perform();
        }
        return working;
    }

    /* -----------------------------------------------------------
     * Internal utilities
     * -----------------------------------------------------------
     */

    private ItemStack getBaseStack() {
        ItemStack item;
        if (itemBase == null) {
            item = new ItemStack(RegistryInventory.modularItem);
            item.setCount(calculateMaxProduction());
        } else {
            item = itemBase.copy();
        }
        return item;
    }

    private CraftAction createAction(ItemStack working, PipelineEntry e) {
        CraftAction action = new CraftAction(
                working,
                new SlotProperty.ModuleSlot(),
                e.module,
                player,
                null,
                new HashMap<>(),
                null
        );
        action.slotLocation.clear();
        action.slotLocation.addAll(e.moduleSlot);
        return action;
    }

    private record PipelineEntry(
            List<String> moduleSlot,
            ItemModule module,
            int slotIndex,
            Container container
    ) {
    }
}
