package smartin.miapi.stat.stats;

import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;
import smartin.miapi.stat.api.data.BooleanStatData;
import smartin.miapi.stat.api.data.number.DoubleStatData;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.FireProof;
import smartin.miapi.modules.properties.LuminousLearningProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@SuppressWarnings("unused")
public class MiscStats {
    public final Stat<Double, AttributeStatData>[] SWIM_SPEED =
            new AttributeStatBuilder(
                    Miapi.id("swim_speed"),
                    StatGroups.MISC,
                    AttributeRegistry.SWIM_SPEED
            ).setTranslationKey("miapi.swim_speed")
                    .setMax(1.5)
                    .setDefault(1)
                    .setMin(0)
                    .register();

    public final Stat<Double, AttributeStatData>[] ELYTRA_GLIDE_EFFICIENCY =
            new AttributeStatBuilder(
                    Miapi.id("elytra_glide_efficiency"),
                    StatGroups.MISC,
                    AttributeRegistry.ELYTRA_GLIDE_EFFICIENCY
            ).setTranslationKey("miapi.elytra_glide")
                    .setMax(20)
                    .setMin(-20)
                    .register();

    public final Stat<Double, AttributeStatData>[] ELYTRA_TURN_EFFICIENCY =
            new AttributeStatBuilder(
                    Miapi.id("elytra_turn_efficiency"),
                    StatGroups.MISC,
                    AttributeRegistry.ELYTRA_TURN_EFFICIENCY
            ).setTranslationKey("miapi.elytra_turn")
                    .setMax(20)
                    .setMin(-20)
                    .register();

    public final Stat<Double, AttributeStatData>[] ELYTRA_ROCKET_EFFICIENCY =
            new AttributeStatBuilder(
                    Miapi.id("elytra_rocket_efficiency"),
                    StatGroups.MISC,
                    AttributeRegistry.ELYTRA_ROCKET_EFFICIENCY
            ).setTranslationKey("miapi.rocket_efficiency")
                    .setMax(5)
                    .setMin(-5)
                    .register();

    public final Stat<Double, AttributeStatData>[] PLAYER_ITEM_USE_MOVEMENT_SPEED =
            new AttributeStatBuilder(
                    Miapi.id("player_item_use_movement_speed"),
                    StatGroups.MISC,
                    AttributeRegistry.PLAYER_ITEM_USE_MOVEMENT_SPEED
            ).setTranslationKey("miapi.player_item_use_speed")
                    .setMax(0)
                    .setMin(-1)
                    .register();

    public final Stat<Double, AttributeStatData>[] LUCK =
            new AttributeStatBuilder(
                    Miapi.id("luck"),
                    StatGroups.MISC,
                    Attributes.LUCK
            ).setMax(5)
                    .setMin(0)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> DURABILITY =
            DoubleStatData.forProperty(
                            DurabilityProperty.KEY,
                            StatGroups.MISC,
                            DurabilityProperty.property
                    ).setMax(2000)
                    .setFormat("##")
                    .setPriority(20)
                    .register();

    public final Stat<DoubleOperationResolvable, DoubleStatData> LUMINOUS_LEARNING =
            DoubleStatData.forProperty(
                            LuminousLearningProperty.KEY,
                            StatGroups.MISC,
                            LuminousLearningProperty.property
                    ).setMax(2)
                    .setFormat("##.#")
                    .register();

    public final Stat<Boolean, BooleanStatData> FIRE_PROOF =
            BooleanStatData.forProperty(
                    FireProof.KEY,
                    StatGroups.MISC,
                    FireProof.property
            ).register();
}
