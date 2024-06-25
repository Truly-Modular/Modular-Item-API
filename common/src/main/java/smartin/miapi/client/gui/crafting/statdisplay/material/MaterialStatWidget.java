package smartin.miapi.client.gui.crafting.statdisplay.material;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

public class MaterialStatWidget extends InteractAbleWidget {


    public MaterialStatWidget(Material material, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        List<String> strings = material.getAllDisplayPropertyKeys();
        List<InteractAbleWidget> widgets = new ArrayList<>();
        for (String propertyKey : strings) {
            ModuleInstance moduleInstance = new ModuleInstance(ItemModule.internal);
            ItemStack compareMaterial = RegistryInventory.modularItem.getDefaultStack();
            /*
            MaterialProperty.setMaterial(moduleInstance, material.getKey());
            JsonArray jsonElements = new JsonArray();
            jsonElements.add(propertyKey);
            JsonObject object = Miapi.gson.fromJson(moduleInstance.moduleData.get("properties"), JsonObject.class);
            object.add(MaterialProperties.KEY, jsonElements);
            moduleInstance.moduleData.put("properties", Miapi.gson.toJson(object));
            moduleInstance.writeToItem(compareMaterial);

             */

            ModuleDataPropertiesManager.setProperties(moduleInstance, material.getDisplayMaterialProperties(propertyKey));
            moduleInstance.writeToItem(compareMaterial);
            ModularItemCache.clearUUIDFor(compareMaterial);
            StatListWidget.setAttributeCaches(compareMaterial, compareMaterial);
            List<InteractAbleWidget> stats = StatListWidget.collectWidgets(compareMaterial, compareMaterial);
            if (!stats.isEmpty()) {
                widgets.add(new MaterialGroupTitleWidget(0, 0, width, propertyKey));
                widgets.addAll(stats);
            }
        }
        ScrollList list = new ScrollList(x, y, width, height, widgets);
        list.alwaysEnableScrollbar = true;
        list.altDesign = true;
        addChild(list);
    }
}
