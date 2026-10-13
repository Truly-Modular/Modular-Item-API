package smartin.miapi.stat.stats;

import net.minecraft.network.chat.Component;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.onHit.*;
import smartin.miapi.modules.properties.onHit.entity.AquaticDamage;
import smartin.miapi.modules.properties.onHit.entity.IllagerBane;
import smartin.miapi.modules.properties.onHit.entity.SmiteDamage;
import smartin.miapi.modules.properties.onHit.entity.SpiderDamage;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.text.DecimalFormat;

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

    public final Stat<DoubleOperationResolvable, DoubleStatData> NEMESIS =
            DoubleStatData.forProperty(
                            NemesisProperty.KEY,
                            StatGroups.ON_HIT,
                            NemesisProperty.property
                    ).setMax(1)
                    .setPriority(6)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> HEALTH_PERCENT_DAMAGE =
            DoubleStatData.forProperty(
                            HealthPercentDamage.KEY,
                            StatGroups.ON_HIT,
                            HealthPercentDamage.property
                    ).setMax(50)
                    .setPriority(1)
                    .setDescription((stack, data, meta) ->
                            Component.translatable(
                                    Miapi.MOD_ID + ".stat." +
                                    Miapi.toLangString(HealthPercentDamage.KEY) +
                                    ".description",
                                    new DoubleStatData.NumberWrapper(
                                            HealthPercentDamage.property
                                                    .getValue(stack)
                                                    .orElse(0.0) / 100.0,
                                            new DecimalFormat("##.##")
                                    )
                            )
                    )
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> FRACTURING =
            DoubleStatData.forProperty(
                            FracturingProperty.KEY,
                            StatGroups.ON_HIT,
                            FracturingProperty.property
                    ).setMax(50)
                    .setPriority(7)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> IMMOLATE =
            DoubleStatData.forProperty(
                            ImmolateProperty.KEY,
                            StatGroups.ON_HIT,
                            ImmolateProperty.property
                    ).setMax(4)
                    .setPriority(3)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> LEECHING =
            DoubleStatData.forProperty(
                            LeechingProperty.KEY,
                            StatGroups.ON_HIT,
                            LeechingProperty.property
                    ).setMax(2)
                    .setPriority(8)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ILLAGER_BANE =
            DoubleStatData.forProperty(
                            IllagerBane.KEY,
                            StatGroups.ON_HIT,
                            IllagerBane.property
                    ).setMax(3)
                    .setFormat("##.#")
                    .setPriority(5)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> AQUATIC_DAMAGE =
            DoubleStatData.forProperty(
                            AquaticDamage.KEY,
                            StatGroups.ON_HIT,
                            AquaticDamage.property
                    ).setMax(5)
                    .setPriority(9)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> SPIDER_DAMAGE =
            DoubleStatData.forProperty(
                            SpiderDamage.KEY,
                            StatGroups.ON_HIT,
                            SpiderDamage.property
                    ).setMax(5)
                    .setPriority(7)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> SMITE_DAMAGE =
            DoubleStatData.forProperty(
                            SmiteDamage.KEY,
                            StatGroups.ON_HIT,
                            SmiteDamage.property
                    ).setMax(5)
                    .setPriority(10)
                    .register();
}