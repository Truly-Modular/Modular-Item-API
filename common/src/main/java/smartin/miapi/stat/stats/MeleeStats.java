package smartin.miapi.stat.stats;

import net.minecraft.world.entity.ai.attributes.Attributes;
import smartin.miapi.Miapi;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.data.attribute.AttributeStatBuilder;
import smartin.miapi.stat.api.data.attribute.AttributeStatData;

@SuppressWarnings("unused")
public class MeleeStats {
    public final Stat<Double, AttributeStatData>[] ATTACK_DAMAGE =
            new AttributeStatBuilder(
                    Miapi.id("attack_damage"),
                    StatGroups.MELEE_GROUP,
                    Attributes.ATTACK_DAMAGE
            ).setTranslationKey("minecraft.damage")
                    .setDefault(1)
                    .setMax(13.0)
                    .setPriority(0)
                    .register();

    public final Stat<Double, AttributeStatData>[] ATTACK_SPEED =
            new AttributeStatBuilder(
                    Miapi.id("attack_speed"),
                    StatGroups.MELEE_GROUP,
                    Attributes.ATTACK_SPEED
            ).setTranslationKey("minecraft.attack_speed")
                    .setFallback(0.0)
                    .setDefault(4)
                    .setMax(4.0)
                    .setPriority(1)
                    .register();

    public final Stat<Double, AttributeStatData>[] ENTITY_INTERACTION_RANGE =
            new AttributeStatBuilder(
                    Miapi.id("entity_interaction_range"),
                    StatGroups.MELEE_GROUP,
                    Attributes.ENTITY_INTERACTION_RANGE
            ).setTranslationKey("minecraft.attack_range")
                    .setDefault(0)
                    .setFormat("##.##")
                    .setMax(2)
                    .setPriority(3)
                    .register();
}