package smartin.miapi.stat.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record StatGroupData(
        double priority
) {
    public static final Codec<StatGroupData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.optionalFieldOf("priority", 0D)
                            .forGetter(StatGroupData::priority)
            ).apply(instance, StatGroupData::new));

    public StatGroup setupAndRegister(ResourceLocation id) {
        StatGroup existing = StatGroup.STAT_GROUP_REGISTRY.get(id);

        if (existing != null) {
            return existing;
        }

        return StatGroup.STAT_GROUP_REGISTRY.register(
                id,
                new StatGroup(id, priority)
        );
    }
}