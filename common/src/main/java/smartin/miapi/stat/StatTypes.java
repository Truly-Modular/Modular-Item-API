package smartin.miapi.stat;

import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.api.*;
import smartin.miapi.stat.api.data.BooleanStatData;
import smartin.miapi.stat.api.data.attribute.AttributeGenerator;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.attribute.AttributeStatDataHandler;
import smartin.miapi.stat.api.data.mining.MiningStat;
import smartin.miapi.stat.api.data.mining.MiningStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.stat.api.data.number.DoubleStatDataHandler;
import smartin.miapi.stat.stats.*;

public class StatTypes {
    public static final StatType<MiningStat, MiningStatData> PICKAXE_MINING = StatType.register(Miapi.id("mining"), MiningStat.class, MiningStatData.class);
    public static final StatType<DoubleOperationResolvable, DoubleStatData> DOUBLE_RESOLVEABLE_TYPE = StatType.register(Miapi.id("double"), DoubleOperationResolvable.class, DoubleStatData.class);
    public static final StatType<Double, AttributeStatData> ATTRIBUTE_TYPE = StatType.register(Miapi.id("attribute"), Double.class, AttributeStatData.class);
    public static final StatType<Boolean, BooleanStatData> BOOLEAN_TYPE = StatType.register(Miapi.id("boolean"), Boolean.class, BooleanStatData.class);

    public static final StatDecoder.Type<?, ?> ATTRIBUTE_STAT = StatDecoder.register(Miapi.id("attribute"), AttributeStatDataHandler.CODEC);
    public static final StatDecoder.Type<?, ?> PROPERTY_STAT = StatDecoder.register(Miapi.id("property"), DoubleStatDataHandler.CODEC);


    public static void setup() {
        new ArmorStats();
        new MeleeModifierStats();
        new MeleeStats();
        new MiningStats();
        new MiscStats();
        new ProjectileModifierStats();
        new ProjectileWeaponStats();
        new ThrowStats();
        StatAggregator.AGGREGATE_STATS_EVENT.register((group, helper, baseItem, compareItem) ->
                Stat.STAT_REGISTRY.getFlatMap().values()
                        .stream().filter(stat -> stat.group().equals(group))
                        .forEach(stat -> {
                            if (stat.metaData().shouldBeVisible(baseItem, compareItem)) {
                                helper.addStat(new StatValue.StatWithValues<>(stat, baseItem, compareItem));
                            }
                        }));
        AttributeGenerator.setup();
    }
}
