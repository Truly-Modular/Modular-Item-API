package smartin.miapi.client.gui.crafting.statdisplay.rework.stats;

import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.crafting.statdisplay.rework.StatGroups;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.attribute.AttributeStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.BooleanStatData;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number.DoubleStatData;
import smartin.miapi.modules.properties.armor.CanWalkOnSnow;
import smartin.miapi.modules.properties.armor.IsPiglinGold;
import smartin.miapi.modules.properties.armor.PillagesGuard;
import smartin.miapi.modules.properties.armor.StepCancelingProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@SuppressWarnings("unused")
public class ArmorStats {
    public final Stat<Double, AttributeStatData>[] ARMOR =
            new AttributeStatBuilder(
                    Miapi.id("armor"),
                    StatGroups.ARMOR,
                    Attributes.ARMOR
            ).setTranslationKey("minecraft.armor")
                    .setMax(8)
                    .setPriority(0)
                    .build();

    public final Stat<Double, AttributeStatData>[] PROJECTILE_ARMOR =
            new AttributeStatBuilder(
                    Miapi.id("projectile_armor"),
                    StatGroups.ARMOR,
                    AttributeRegistry.PROJECTILE_ARMOR
            ).setTranslationKey("miapi.projectile_armor")
                    .setMax(8)
                    .setPriority(1)
                    .build();

    public final Stat<Double, AttributeStatData>[] ARMOR_TOUGHNESS =
            new AttributeStatBuilder(
                    Miapi.id("armor_toughness"),
                    StatGroups.ARMOR,
                    Attributes.ARMOR_TOUGHNESS
            ).setTranslationKey("minecraft.armor_toughness")
                    .setMax(3)
                    .setPriority(2)
                    .build();

    public final Stat<Double, AttributeStatData>[] SHIELDING_ARMOR =
            new AttributeStatBuilder(
                    Miapi.id("shielding_armor"),
                    StatGroups.ARMOR,
                    AttributeRegistry.SHIELDING_ARMOR
            ).setTranslationKey("miapi.shielding_armor")
                    .setMax(4)
                    .setPriority(3)
                    .build();

    public final Stat<Double, AttributeStatData>[] KNOCKBACK_RESISTANCE =
            new AttributeStatBuilder(
                    Miapi.id("knockback_resistance"),
                    StatGroups.ARMOR,
                    Attributes.KNOCKBACK_RESISTANCE
            ).setTranslationKey("minecraft.knockback_resistance")
                    .setMax(1)
                    .setPriority(4)
                    .build();

    public final Stat<DoubleOperationResolvable, DoubleStatData> PILLAGES_GUARD =
            DoubleStatData.forProperty(
                            PillagesGuard.KEY,
                            StatGroups.ARMOR,
                            PillagesGuard.property
                    ).setMax(3)
                    .setFormat("##.#")
                    .setPriority(5)
                    .setDescription((stack, data, meta) -> {
                        double value = PillagesGuard.valueRemap(
                                PillagesGuard.property.getValue(stack).orElse(0.0)
                        );
                        value = (double) Math.round((1 - value) * 1000) / 10;
                        return net.minecraft.network.chat.Component.translatable(
                                "miapi.stat.pillagerGuard.description",
                                value
                        );
                    })
                    .build();

    public final Stat<Boolean, BooleanStatData> CAN_WALK_ON_SNOW =
            BooleanStatData.forProperty(
                    CanWalkOnSnow.KEY,
                    StatGroups.ARMOR,
                    CanWalkOnSnow.property
            ).setPriority(6).build();

    public final Stat<Boolean, BooleanStatData> IS_PIGLIN_GOLD =
            BooleanStatData.forProperty(
                    IsPiglinGold.KEY,
                    StatGroups.ARMOR,
                    IsPiglinGold.property
            ).setPriority(7).build();

    public final Stat<Boolean, BooleanStatData> STEP_CANCELING =
            BooleanStatData.forProperty(
                    StepCancelingProperty.KEY,
                    StatGroups.ARMOR,
                    StepCancelingProperty.property
            ).setPriority(8).build();
}