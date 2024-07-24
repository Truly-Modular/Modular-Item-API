package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.JsonStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiStatProperty extends CodecProperty<Map<String, GuiStatProperty.GuiInfo>> {
    public static String KEY = "gui_stat";
    public static GuiStatProperty property;
    public static Codec<Map<String, GuiInfo>> CODEC = Codec.dispatchedMap(Codec.STRING, (key) -> AutoCodec.of(GuiInfo.class).codec());

    public GuiStatProperty() {
        super(CODEC);
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
                                gui.min.getValue(),
                                gui.max.getValue()

                        );
                        combined.add((T) display);
                    });
                    return combined;
                }
            });
        }
    }

    private static Map<String, GuiInfo> getInfoCache(ItemStack itemStack) {
        return property.getData(itemStack).orElse(new HashMap<>());
    }

    public static Map<String, GuiInfo> getInfo(ItemStack itemStack) {
        return ModularItemCache.getVisualOnlyCache(itemStack, KEY, new HashMap<>());
    }

    public static double getValue(ItemStack itemStack, String key) {
        Map<String, GuiInfo> infoMap = getInfoCache(itemStack);
        if (infoMap.containsKey(key)) {
            return infoMap.get(key).value.getValue();
        }
        return 0.0;
    }

    @Override
    public Map<String, GuiInfo> merge(Map<String, GuiInfo> left, Map<String, GuiInfo> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left,right,mergeType);
    }

    @Override
    public Map<String, GuiInfo> initialize(Map<String, GuiInfo> property, ModuleInstance context) {
        Map<String, GuiInfo> initialied = new HashMap<>();
        property.forEach((key, value) -> initialied.put(key, value.initialize(context)));
        return super.initialize(property, context);
    }

    public static class GuiInfo {
        @CodecBehavior.Optional
        public DoubleOperationResolvable min = new DoubleOperationResolvable(0.0);
        @CodecBehavior.Optional
        public DoubleOperationResolvable max = new DoubleOperationResolvable(10.0);
        public DoubleOperationResolvable value;
        public Component header;
        @CodecBehavior.Optional
        @Nullable
        public Component description;

        public GuiInfo initialize(ModuleInstance moduleInstance) {
            GuiInfo init = new GuiInfo();
            init.min = this.min.initialize(moduleInstance);
            init.max = this.max.initialize(moduleInstance);
            init.value = this.value.initialize(moduleInstance);
            init.header = this.header;
            init.description = this.description;
            return init;
        }
    }
}
