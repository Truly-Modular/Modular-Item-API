package smartin.miapi.stat.stats;

import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.BooleanStatData;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.mining.MiningStat;
import smartin.miapi.stat.api.data.mining.MiningStatData;
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
            ).register();

    public final Stat<Double, AttributeStatData>[] BLOCK_INTERACTION_RANGE =
            new AttributeStatBuilder(
                    Miapi.id("block_interaction_range"),
                    StatGroups.MINING_GROUP,
                    Attributes.BLOCK_INTERACTION_RANGE
            ).setTranslationKey("minecraft.reach")
                    .setDefault(0)
                    .setFormat("##.##")
                    .setMax(2)
                    .setPriority(2)
                    .register();
}
