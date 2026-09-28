package smartin.miapi.client.gui.crafting.statdisplay.rework.stats;

import smartin.miapi.client.gui.crafting.statdisplay.rework.StatGroups;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.BooleanStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.mining.MiningStat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.mining.MiningStatData;
import smartin.miapi.modules.properties.mining.MiningTelekinesisProperty;

@SuppressWarnings("unused")
public class MiningStats {
    public final Stat<MiningStat, MiningStatData> PICKAXE_MINING =
            MiningStatData.getMiningStat("pickaxe");

    public final Stat<MiningStat, MiningStatData> AXE_MINING =
            MiningStatData.getMiningStat("axe");

    public final Stat<MiningStat, MiningStatData> SHOVEL_MINING =
            MiningStatData.getMiningStat("shovel");

    public final Stat<MiningStat, MiningStatData> HOE_MINING =
            MiningStatData.getMiningStat("hoe");

    public final Stat<MiningStat, MiningStatData> SWORD_MINING =
            MiningStatData.getMiningStat("sword");

    public final Stat<Boolean, BooleanStatData> MINING_TELEKINESIS =
            BooleanStatData.forProperty(
                    MiningTelekinesisProperty.KEY,
                    StatGroups.MINING_GROUP,
                    MiningTelekinesisProperty.property
            ).build();
}
