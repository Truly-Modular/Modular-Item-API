package smartin.miapi.client.gui.crafting.statdisplay.rework.stats;

import smartin.miapi.client.gui.crafting.statdisplay.rework.StatGroups;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.BooleanStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number.DoubleStatData;
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
                    .build();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ARROW_RETRIEVAL =
            DoubleStatData.forProperty(
                    ArrowRetrievalProperty.KEY,
                    StatGroups.PROJECTILE_WEAPON,
                    ArrowRetrievalProperty.property
            ).setMax(1.0)
                    .setFormat("0%")
                    .build();

    public final Stat<Boolean, BooleanStatData> IS_CROSSBOW_SHOOTABLE =
            BooleanStatData.forProperty(
                    IsCrossbowShootAble.KEY,
                    StatGroups.PROJECTILE_WEAPON,
                    IsCrossbowShootAble.property
            ).build();

    public final Stat<Boolean, BooleanStatData> MAKES_IMPACT_SOUND =
            BooleanStatData.forProperty(
                    MakesImpactSoundProperty.KEY,
                    StatGroups.PROJECTILE,
                    MakesImpactSoundProperty.property
            ).build();
}
