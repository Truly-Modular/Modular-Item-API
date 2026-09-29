package smartin.miapi.stat.stats;

import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.BooleanStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.modules.properties.projectile.ArrowRetrievalProperty;
import smartin.miapi.modules.properties.projectile.IsCrossbowShootAble;
import smartin.miapi.modules.properties.projectile.MakesImpactSoundProperty;
import smartin.miapi.modules.properties.projectile.RapidfireCrossbowProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@SuppressWarnings("unused")
public class ProjectileModifierStats {
    public final Stat<DoubleOperationResolvable, DoubleStatData> RAPIDFIRE_CROSSBOW =
            DoubleStatData.forProperty(
                            RapidfireCrossbowProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            RapidfireCrossbowProperty.property
                    ).setMax(3)
                    .setFormat("##")
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ARROW_RETRIEVAL =
            DoubleStatData.forProperty(
                            ArrowRetrievalProperty.KEY,
                            StatGroups.PROJECTILE_WEAPON,
                            ArrowRetrievalProperty.property
                    ).setMax(1.0)
                    .setFormat("0%")
                    .register();

    public final Stat<Boolean, BooleanStatData> IS_CROSSBOW_SHOOTABLE =
            BooleanStatData.forProperty(
                    IsCrossbowShootAble.KEY,
                    StatGroups.PROJECTILE_WEAPON,
                    IsCrossbowShootAble.property
            ).register();

    public final Stat<Boolean, BooleanStatData> MAKES_IMPACT_SOUND =
            BooleanStatData.forProperty(
                    MakesImpactSoundProperty.KEY,
                    StatGroups.PROJECTILE_MODIFIER,
                    MakesImpactSoundProperty.property
            ).setDefaultValue(true).register();
}
