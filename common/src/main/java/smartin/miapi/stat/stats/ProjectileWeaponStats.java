package smartin.miapi.stat.stats;

import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.modules.properties.projectile.stat.bow.BowAccuracyProperty;
import smartin.miapi.modules.properties.projectile.stat.bow.BowDrawTimeProperty;
import smartin.miapi.modules.properties.projectile.stat.bow.BowSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@SuppressWarnings("unused")
public class ProjectileWeaponStats {
    public final Stat<DoubleOperationResolvable, DoubleStatData> BOW_SPEED =
            DoubleStatData.forProperty(
                            BowSpeedProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            BowSpeedProperty.property
                    ).setMax(0.5)
                    .setFormat("0.##'%'")
                    .setPriority(-10)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> BOW_DRAW_TIME =
            DoubleStatData.forProperty(
                            BowDrawTimeProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            BowDrawTimeProperty.property
                    ).setMax(2.5)
                    .setFormat("##.##")
                    .setInverse(true)
                    .setPriority(-9)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> BOW_ACCURACY =
            DoubleStatData.forProperty(
                            BowAccuracyProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            BowAccuracyProperty.property
                    ).setMax(0.5)
                    .setFormat("0.##'%'")
                    .setPriority(-8)
                    .register();
}
