package smartin.miapi.client.gui.crafting.statdisplay.rework.stats;

import smartin.miapi.client.gui.crafting.statdisplay.rework.StatGroups;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number.DoubleStatData;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@SuppressWarnings("unused")
public class ThrowStats {
    public final Stat<DoubleOperationResolvable, DoubleStatData> THROW_SPEED =
            DoubleStatData.forProperty(
                            ThrowSpeedProperty.KEY,
                            StatGroups.THROW,
                            ThrowSpeedProperty.property
                    ).setMax(5.0)
                    .setFormat("##.##")
                    .setPriority(-10)
                    .build();

    public final Stat<DoubleOperationResolvable, DoubleStatData> THROW_DAMAGE =
            DoubleStatData.forProperty(
                            ThrowDamageProperty.KEY,
                            StatGroups.THROW,
                            ThrowDamageProperty.property
                    ).setMax(6)
                    .setFormat("##.##")
                    .setPriority(-9)
                    .build();
}
