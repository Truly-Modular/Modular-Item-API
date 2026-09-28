package smartin.miapi.client.gui.crafting.statdisplay.rework;

import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatDecoder;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatType;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.BooleanStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.attribute.AttributeStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.attribute.AttributeStatDataHandler;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.mining.MiningStat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.mining.MiningStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number.DoubleStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number.DoubleStatDataHandler;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

public class StatTypes {
    public static final StatType<MiningStat, MiningStatData> PICKAXE_MINING = StatType.register(Miapi.id("mining"), MiningStat.class, MiningStatData.class);
    public static final StatType<DoubleOperationResolvable, DoubleStatData> DOUBLE_RESOLVEABLE_TYPE = StatType.register(Miapi.id("double"), DoubleOperationResolvable.class, DoubleStatData.class);
    public static final StatType<Double, AttributeStatData> ATTRIBUTE_TYPE = StatType.register(Miapi.id("attribute"), Double.class, AttributeStatData.class);
    public static final StatType<Boolean, BooleanStatData> BOOLEAN_TYPE = StatType.register(Miapi.id("boolean"), Boolean.class, BooleanStatData.class);

    public static final StatDecoder.Type<?, ?> ATTRIBUTE_STAT = StatDecoder.register(Miapi.id("attribute"), AttributeStatDataHandler.CODEC);
    public static final StatDecoder.Type<?, ?> PROPERTY_STAT = StatDecoder.register(Miapi.id("property"), DoubleStatDataHandler.CODEC);
}
