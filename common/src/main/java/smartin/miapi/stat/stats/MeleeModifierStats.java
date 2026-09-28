package smartin.miapi.stat.stats;

import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.onHit.*;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.text.DecimalFormat;

@SuppressWarnings("unused")
public class MeleeModifierStats {
    public final Stat<Double, AttributeStatData>[] BACK_STAB =
            new AttributeStatBuilder(
                    Miapi.id("back_stab"),
                    StatGroups.MELEE_MODIFIER_GROUP,
                    AttributeRegistry.BACK_STAB
            ).setTranslationKey("miapi.back_stab")
                    .setDefault(1)
                    .setFormat("##.#")
                    .setMax(5)
                    .setPriority(0)
                    .register();

    public final Stat<Double, AttributeStatData>[] CRITICAL_DAMAGE =
            new AttributeStatBuilder(
                    Miapi.id("critical_damage"),
                    StatGroups.MELEE_MODIFIER_GROUP,
                    AttributeRegistry.CRITICAL_DAMAGE
            ).setTranslationKey("miapi.crit_damage")
                    .setMin(0)
                    .setMax(3)
                    .setPriority(1)
                    .register();

    public final Stat<Double, AttributeStatData>[] CRITICAL_CHANCE =
            new AttributeStatBuilder(
                    Miapi.id("critical_chance"),
                    StatGroups.MELEE_MODIFIER_GROUP,
                    AttributeRegistry.CRITICAL_CHANCE
            ).setTranslationKey("miapi.crit_chance")
                    .setMin(0)
                    .setMax(1)
                    .setPriority(2)
                    .register();

    public final Stat<Double, AttributeStatData>[] SHIELD_BREAK =
            new AttributeStatBuilder(
                    Miapi.id("shield_break"),
                    StatGroups.MELEE_MODIFIER_GROUP,
                    AttributeRegistry.SHIELD_BREAK
            ).setTranslationKey("miapi.shield_break")
                    .setDefault(0)
                    .setFormat("##.#")
                    .setMax(5)
                    .setPriority(3)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> ARMOR_PENETRATION =
            DoubleStatData.forProperty(
                            ArmorPenProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            ArmorPenProperty.property
                    ).setMin(-20)
                    .setMax(50)
                    .setPriority(4)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> SLASHING =
            DoubleStatData.forProperty(
                            SlashingProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            SlashingProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(5)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> BLUDGEON =
            DoubleStatData.forProperty(
                            BludgeonProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            BludgeonProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(6)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> EXECUTIONER =
            DoubleStatData.forProperty(
                            ExecutionerProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            ExecutionerProperty.property
                    ).setMin(-2)
                    .setMax(12)
                    .setPriority(7)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> INERTIA =
            DoubleStatData.forProperty(
                            InertiaProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            InertiaProperty.property
                    ).setMax(10)
                    .setPriority(8)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> COMBO =
            DoubleStatData.forProperty(
                            ComboProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            ComboProperty.property
                    ).setMax(10)
                    .setPriority(9)
                    .setDescription((stack, data, meta) -> {
                        var format = new DecimalFormat("##.##");
                        var intFormat = new DecimalFormat("##");
                        return net.minecraft.network.chat.Component.translatable(
                                Miapi.MOD_ID + ".stat." + Miapi.toLangString(ComboProperty.KEY),
                                intFormat.format(ComboProperty.property.getValue(stack).orElse(0.0)),
                                format.format(ComboTimeProperty.property.getValue(stack).orElse(0.0) / 20)
                        );
                    })
                    .register();
}