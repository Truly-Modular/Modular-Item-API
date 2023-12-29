package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.JsonStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class GuiWidgetProperty<T extends InteractAbleWidget & SingleStatDisplay> implements StatListWidget.StatWidgetSupplier, ModuleProperty {

    public GuiWidgetProperty() {
        StatListWidget.jsonConverterMap.put("double", ((element, readerHelper) -> {
            JsonObject object = element.getAsJsonObject();
            Text header = Codecs.TEXT.parse(JsonOps.INSTANCE, object.get("header")).result().orElse(Text.empty());
            Text description = Codecs.TEXT.parse(JsonOps.INSTANCE, object.get("description")).result().orElse(Text.empty());
            double min = getOrDefault(object, "min", 0);
            double max = getOrDefault(object, "max", 10);
            return new JsonStatDisplay(
                    stack -> header,
                    stack -> description,
                    readerHelper,
                    min,
                    max
            );
        }));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public List<T> currentList(ItemStack original, ItemStack compareTo) {
        List<T> guis = getGuis(original);
        guis.addAll(getGuis(compareTo));
        return guis;
    }

    @Environment(EnvType.CLIENT)
    public List<T> getGuis(ItemStack itemStack) {
        List<T> guis = new ArrayList<>();
        JsonElement element = ItemModule.getModules(itemStack).getPropertiesMerged().get(this);
        if (element != null && element.isJsonArray()) {
            element.getAsJsonArray().forEach(guiElement -> {
                if (guiElement instanceof JsonObject object) {
                    if (object.has("type")) {
                        String type = object.get("type").getAsString();
                        if (StatListWidget.jsonConverterMap.containsKey(type)) {
                            double getValue = getOrDefault(object, "default", 0.0d);
                            guis.add((T) StatListWidget.jsonConverterMap.get(type).fromJson(guiElement, new SingleStatDisplayDouble.StatReaderHelper() {
                                @Override
                                public double getValue(ItemStack testStack) {
                                    if (itemStack.equals(testStack)) {
                                        return getValue;
                                    }
                                    return 0;
                                }

                                @Override
                                public boolean hasValue(ItemStack testStack) {
                                    return itemStack.equals(testStack);
                                }
                            }));
                        }
                    }
                }
            });
        }
        return guis;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    public double getOrDefault(JsonObject element, String key, double fallback) {
        if (element.has(key) && element.get(key).isJsonPrimitive()) {
            return element.get(key).getAsDouble();
        }
        return fallback;
    }
}
