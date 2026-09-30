package smartin.miapi.stat.stats;

import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.onHit.ArmorPenProperty;
import smartin.miapi.modules.properties.onHit.BludgeonProperty;
import smartin.miapi.modules.properties.onHit.ExecutionerProperty;
import smartin.miapi.modules.properties.onHit.SlashingProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;

@SuppressWarnings("unused")
public class OnHitStats {
    public final Stat<Double, AttributeStatData>[] CRITICAL_DAMAGE =
            new AttributeStatBuilder(
                    Miapi.id("critical_damage"),
                    StatGroups.ON_HIT,
                    AttributeRegistry.CRITICAL_DAMAGE
            ).setTranslationKey("miapi.crit_damage")
                    .setMin(0)
                    .setMax(3)
                    .setPriority(0)
                    .register();

    public final Stat<Double, AttributeStatData>[] CRITICAL_CHANCE =
            new AttributeStatBuilder(
                    Miapi.id("critical_chance"),
                    StatGroups.ON_HIT,
                    AttributeRegistry.CRITICAL_CHANCE
            ).setTranslationKey("miapi.crit_chance")
                    .setMin(0)
                    .setMax(1)
                    .setPriority(1)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ARMOR_PENETRATION =
            DoubleStatData.forProperty(
                            ArmorPenProperty.KEY,
                            StatGroups.ON_HIT,
                            ArmorPenProperty.property
                    ).setMin(-20)
                    .setMax(50)
                    .setPriority(2)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> SLASHING =
            DoubleStatData.forProperty(
                            SlashingProperty.KEY,
                            StatGroups.ON_HIT,
                            SlashingProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(3)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> BLUDGEON =
            DoubleStatData.forProperty(
                            BludgeonProperty.KEY,
                            StatGroups.ON_HIT,
                            BludgeonProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(4)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> EXECUTIONER =
            DoubleStatData.forProperty(
                            ExecutionerProperty.KEY,
                            StatGroups.ON_HIT,
                            ExecutionerProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(5)
                    .register();
}