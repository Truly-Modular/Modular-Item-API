package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.Environment;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.JsonStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiStatProperty implements ModuleProperty {
    public static String KEY = "gui_stat";
    public static GuiStatProperty property;

    public GuiStatProperty() {
        property = this;
        if (Environment.isClient()) {
            ModularItemCache.setSupplier(KEY, GuiStatProperty::getInfoCache);
            StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
                @Override
                public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                    List<T> combined = new ArrayList<>();
                    Map<String, GuiInfo> combinedMap = getInfo(original);
                    combinedMap.putAll(getInfo(compareTo));
                    combinedMap.forEach((key, gui) -> {
                        JsonStatDisplay display = new JsonStatDisplay(
                                (itemStack) -> gui.header,
                                (itemStack) -> gui.description,
                                new SingleStatDisplayDouble.StatReaderHelper() {
                                    @Override
                                    public double getValue(ItemStack itemStack) {
                                        return GuiStatProperty.getValue(itemStack, key);
                                    }

                                    @Override
                                    public boolean hasValue(ItemStack itemStack) {
                                        return GuiStatProperty.getValue(itemStack, key) != 0;
                                    }
                                },
                                gui.min,
                                gui.max

                        );
                        combined.add((T) display);
                    });
                    return combined;
                }
            });
        }
    }

    private static Map<String, GuiInfo> getInfoCache(ItemStack itemStack) {
        Map<String, GuiInfo> infoMap = new HashMap<>();
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            if (moduleInstance.getProperties().containsKey(property)) {
                JsonElement element = moduleInstance.getProperties().get(property);
                element.getAsJsonObject().asMap().forEach((id, innerJson) -> {
                    infoMap.put(id, new GuiInfo(innerJson.getAsJsonObject(), moduleInstance));
                });
            }
        }
        return infoMap;
    }

    public static Map<String, GuiInfo> getInfo(ItemStack itemStack) {
        return ModularItemCache.getVisualOnlyCache(itemStack, KEY, new HashMap<>());
    }

    public static double getValue(ItemStack itemStack, String key) {
        Map<String, GuiInfo> infoMap = getInfoCache(itemStack);
        if (infoMap.containsKey(key)) {
            return infoMap.get(key).value;
        }
        return 0.0;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {

        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return ModuleProperty.mergeAsMap(old, toMerge, type);
    }

    public static class GuiInfo {
        public double min;
        public double max;
        public double value;
        public Text header;
        public Text description;

        public GuiInfo(JsonObject json, ModuleInstance moduleInstance) {
            min = ModuleProperty.getDouble(json, "min", moduleInstance, 0.0);
            max = ModuleProperty.getDouble(json, "max", moduleInstance, 3.0);
            value = ModuleProperty.getDouble(json, "value", moduleInstance, 0.0);
            header = ModuleProperty.getText(json, "header", moduleInstance, Text.empty());
            description = ModuleProperty.getText(json, "description", moduleInstance, Text.empty());
        }
    }
}
