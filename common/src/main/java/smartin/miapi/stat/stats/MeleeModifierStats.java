package smartin.miapi.stat.stats;

import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.HandheldItemProperty;
import smartin.miapi.modules.properties.onHit.ComboProperty;
import smartin.miapi.modules.properties.onHit.ComboTimeProperty;
import smartin.miapi.modules.properties.onHit.InertiaProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.BooleanStatData;
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

    public final Stat<Double, AttributeStatData>[] SHIELD_BREAK =
            new AttributeStatBuilder(
                    Miapi.id("shield_break"),
                    StatGroups.MELEE_MODIFIER_GROUP,
                    AttributeRegistry.SHIELD_BREAK
            ).setTranslationKey("miapi.shield_break")
                    .setDefault(0)
                    .setFormat("##.#")
                    .setMax(5)
                    .setPriority(1)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> INERTIA =
            DoubleStatData.forProperty(
                            InertiaProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            InertiaProperty.property
                    ).setMax(10)
                    .setPriority(2)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> COMBO =
            DoubleStatData.forProperty(
                            ComboProperty.KEY,
                            StatGroups.MELEE_MODIFIER_GROUP,
                            ComboProperty.property
                    ).setMax(10)
                    .setPriority(3)
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

    public final Stat<Boolean, BooleanStatData> HANDHELD =
            BooleanStatData.forProperty(
                            HandheldItemProperty.KEY,
                            StatGroups.MISC,
                            HandheldItemProperty.property
                    )
                    .setPriority(4)
                    .register();
}