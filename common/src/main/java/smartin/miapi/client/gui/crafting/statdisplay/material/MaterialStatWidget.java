package smartin.miapi.client.gui.crafting.statdisplay.material;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.mixin.RegistryOpsAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MaterialStatWidget extends InteractAbleWidget {
    ScrollList list;
    Material original;

    public MaterialStatWidget(Material original, Material compareTo, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.original = original;
        list = new ScrollList(getX(), getY(), getWidth(), getHeight(), List.of());
        list.alwaysEnableScrollbar = true;
        list.altDesign = true;
        addChild(list);
        setup(original, compareTo);
    }

    private void setup(Material original, Material compareTo) {
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(original.getAllDisplayPropertyKeys());
        keys.addAll(compareTo.getAllDisplayPropertyKeys());
        List<InteractAbleWidget> widgets = new ArrayList<>();
        for (String propertyKey : keys) {
            ItemStack originalStack = getDisplayStack(original, propertyKey);
            ItemStack compareStack = getDisplayStack(compareTo, propertyKey);
            List<InteractAbleWidget> stats = StatListWidget.collectWidgets(originalStack, compareStack);
            if (!stats.isEmpty()) {
                widgets.add(new MaterialGroupTitleWidget(0, 0, width, propertyKey, stats, originalStack, compareStack));
                widgets.addAll(stats);
            }
        }
        list.setList(widgets);
    }

    public void setCompareTo(ItemStack compareTo) {
        Material compareToMaterial = MaterialProperty.getMaterialFromIngredient(compareTo);
        if (compareToMaterial != null) {
            setup(original, compareToMaterial);
        }
    }

    private static @NotNull ItemStack getDisplayStack(Material original, String propertyKey) {
        ModuleInstance moduleInstance = new ModuleInstance(ItemModule.internal);
        ItemStack compareMaterial = RegistryInventory.modularStackableItem.getDefaultInstance();
        ModuleDataPropertiesManager.setProperties(moduleInstance, original.getDisplayMaterialProperties(propertyKey));
        moduleInstance.clearCaches();
        moduleInstance.writeToItem(compareMaterial);
        moduleInstance.lookup = ((RegistryOpsAccessor) RegistryOps.create(JsonOps.INSTANCE, Miapi.registryAccess)).getLookupProvider();
        moduleInstance.contextStack = compareMaterial;
        moduleInstance.registryAccess = Miapi.registryAccess;
        StatListWidget.setAttributeCaches(compareMaterial, compareMaterial);
        return compareMaterial;
    }
}
