package smartin.miapi.stat.stats;

import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.armor.ExhaustionProperty;
import smartin.miapi.modules.properties.armor.WaterGravityProperty;
import smartin.miapi.modules.properties.projectile.*;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileAccuracyProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.BooleanStatData;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;

@SuppressWarnings("unused")
public class ProjectileModifierStats {
    public final Stat<DoubleOperationResolvable, DoubleStatData> RAPIDFIRE_CROSSBOW =
            DoubleStatData.forProperty(
                            RapidfireCrossbowProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            RapidfireCrossbowProperty.property
                    ).setPriority(0)
                    .setMax(3)
                    .setFormat("##")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ARROW_RETRIEVAL =
            DoubleStatData.forProperty(
                            ArrowRetrievalProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            ArrowRetrievalProperty.property
                    ).setPriority(1)
                    .setMax(1.0)
                    .setFormat("0%")
                    .register();

    public final Stat<Boolean, BooleanStatData> IS_CROSSBOW_SHOOTABLE =
            BooleanStatData.forProperty(
                            IsCrossbowShootAble.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            IsCrossbowShootAble.property
                    ).setPriority(2)
                    .register();

    public final Stat<Boolean, BooleanStatData> MAKES_IMPACT_SOUND =
            BooleanStatData.forProperty(
                            MakesImpactSoundProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            MakesImpactSoundProperty.property
                    ).setPriority(3)
                    .setDefaultValue(true)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> WATER_DRAG =
            DoubleStatData.forProperty(
                            WaterDragProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            WaterDragProperty.property
                    ).setPriority(4)
                    .setMax(1)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> EXHAUSTION =
            DoubleStatData.forProperty(
                            ExhaustionProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            ExhaustionProperty.property
                    ).setPriority(5)
                    .setMax(50)
                    .setFormat("##.#")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> WATER_GRAVITY =
            DoubleStatData.forProperty(
                            WaterGravityProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            WaterGravityProperty.property
                    ).setPriority(6)
                    .setMax(100)
                    .setFormat("##.#")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> PROJECTILE_ACCURACY =
            DoubleStatData.forProperty(
                            ProjectileAccuracyProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            ProjectileAccuracyProperty.property
                    ).setPriority(7)
                    .setMax(0.5)
                    .setFormat("0.##'%'")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> PROJECTILE_DAMAGE =
            DoubleStatData.forProperty(
                            ProjectileDamageProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            ProjectileDamageProperty.property
                    ).setPriority(8)
                    .setMax(0.5)
                    .setFormat("0.##'%'")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> PROJECTILE_SPEED =
            DoubleStatData.forProperty(
                            ProjectileSpeedProperty.KEY,
                            StatGroups.PROJECTILE_MODIFIER,
                            ProjectileSpeedProperty.property
                    ).setPriority(9)
                    .setMax(1.0)
                    .setFormat("0.##'%'")
                    .register();

    public final Stat<Double, AttributeStatData>[] PROJECTILE_PIERCING =
            new AttributeStatBuilder(
                    Miapi.id("projectile_piercing"),
                    StatGroups.PROJECTILE_MODIFIER,
                    AttributeRegistry.PROJECTILE_PIERCING
            ).setTranslationKey("miapi.projectile_piercing")
                    .setDefault(0)
                    .setFormat("##.##")
                    .setMax(10)
                    .register();
}