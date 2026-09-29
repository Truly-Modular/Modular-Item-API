package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.*;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @header GUI Stat Property
 * @path /data_types/properties/gui_stat
 * @description_start The GuiStatProperty manages statistics displayed in the GUI for items. It allows the attachment of custom statistics
 * to items, which can be displayed in the GUI with a header, description, and a range of values. Each statistic is defined
 * with a minimum and maximum value, and the current value is dynamically resolved based on the item instance.
 * This property is used to enhance the user interface by providing detailed item statistics.
 * @description_end
 * @data min: A Double Operation representing the minimum value of the statistic.
 * @data max: A Double Operation representing the maximum value of the statistic.
 * @data value: A Double Operation representing the current value of the statistic.
 * @data header: A Text Element used as the header for the statistic display.
 * @data description: An optional Text Element providing a description for the statistic.
 */

public class GuiStatProperty extends CodecProperty<Map<String, GuiStatProperty.GuiInfo>> {
    public static final ResourceLocation KEY = Miapi.id("gui_stat");
    public static GuiStatProperty property;
    public static Codec<Map<String, GuiInfo>> CODEC = Codec.dispatchedMap(Codec.STRING, (key) -> GuiInfo.CODEC);

    public GuiStatProperty() {
        super(CODEC);
        property = this;
        StatAggregator.AGGREGATE_STATS_EVENT.register((group, helper, baseItem, compareItem) -> {
            Map<String, GuiInfo> combinedMap = new LinkedHashMap<>(getInfo(baseItem));
            combinedMap.putAll(getInfo(compareItem));

            combinedMap.forEach((key, gui) -> {
                StatGroup targetGroup = StatGroup.getOrRegister(gui.group);
                if (targetGroup == group) {
                    helper.addStat(
                            new StatValue.StatWithValues<>(
                                    DoubleStatData.getBuilder(
                                                    Miapi.id("runtime_gui_stat_" + key),
                                                    (item, resolvable, data) -> gui.header,
                                                    (item, resolvable, data) -> gui.description,
                                                    StatGroup.getOrRegister(gui.group),
                                                    item -> gui.value
                                            )
                                            .setMin(gui.min.getValue())
                                            .setMax(gui.max.getValue())
                                            .build(),
                                    baseItem,
                                    compareItem
                            )
                    );
                }
            });
        });
    }

    private static Map<String, GuiInfo> getInfoCache(ItemStack itemStack) {
        return property.getData(itemStack).orElse(new HashMap<>());
    }

    public static Map<String, GuiInfo> getInfo(ItemStack itemStack) {
        return ModularItemCache.getVisualOnlyCache(itemStack, KEY.toString(), new HashMap<>());
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
        return MergeAble.mergeMap(left, right, mergeType, (id, l, r) -> r.merge(l, r, mergeType));
    }

    @Override
    public Map<String, GuiInfo> initialize(Map<String, GuiInfo> property, ModuleInstance context) {
        Map<String, GuiInfo> initialized = new HashMap<>();
        property.forEach((key, value) -> initialized.put(key, value.initialize(context)));
        return super.initialize(initialized, context);
    }

    public static class GuiInfo implements InitializeAble<GuiInfo>, MergeAble<GuiInfo> {
        public DoubleOperationResolvable min = new DoubleOperationResolvable(0.0);
        public DoubleOperationResolvable max = new DoubleOperationResolvable(10.0);
        public DoubleOperationResolvable value;
        public Component header;
        public Component description;
        public ResourceLocation group = Miapi.id("misc");

        public static final Codec<GuiInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("min", new DoubleOperationResolvable(0.0))
                        .forGetter(gui -> gui.min),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("max", new DoubleOperationResolvable(10.0))
                        .forGetter(gui -> gui.max),
                DoubleOperationResolvable.CODEC
                        .fieldOf("value")
                        .forGetter(gui -> gui.value),
                ComponentSerialization.CODEC
                        .fieldOf("header")
                        .forGetter(gui -> gui.header),
                ComponentSerialization.CODEC
                        .optionalFieldOf("description", Component.empty())
                        .forGetter(gui -> gui.description),
                ResourceLocation.CODEC
                        .optionalFieldOf("group", Miapi.id("misc"))
                        .forGetter(gui -> gui.group)
        ).apply(instance, GuiInfo::new));

        public GuiInfo(
                DoubleOperationResolvable min,
                DoubleOperationResolvable max,
                DoubleOperationResolvable value,
                Component header,
                Component description,
                ResourceLocation group
        ) {
            this.min = min;
            this.max = max;
            this.value = value;
            this.header = header;
            this.description = description;
            this.group = group;
        }

        public GuiInfo() {
        }

        public GuiInfo initialize(ModuleInstance moduleInstance) {
            GuiInfo init = new GuiInfo();

            init.min = this.min.initialize(moduleInstance);
            init.max = this.max.initialize(moduleInstance);
            init.value = this.value.initialize(moduleInstance);
            init.header = this.header;
            init.description = this.description;
            init.group = this.group;

            return init;
        }

        @Override
        public GuiInfo initialize(GuiInfo property, ModuleInstance context) {
            return property.initialize(context);
        }

        @Override
        public GuiInfo merge(GuiInfo left, GuiInfo right, MergeType mergeType) {
            GuiInfo init = new GuiInfo();

            init.min = MergeAble.decideLeftRight(left.min, right.min, mergeType);
            init.max = MergeAble.decideLeftRight(left.max, right.max, mergeType);
            init.value = DoubleOperationResolvable.merge(left.value, right.value, mergeType);
            init.header = this.header;
            init.description = this.description;
            init.group = MergeAble.decideLeftRight(left.group, right.group, mergeType);

            return init;
        }
    }
}
