package smartin.miapi.stat.api.data.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.mixin.AttributeAccessor;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.Stat;

public class AttributeGenerator {
    private AttributeGenerator() {

    }

    public static void setup() {
        ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> BuiltInRegistries.ATTRIBUTE.forEach(entityAttribute -> {
            if (entityAttribute != null) {
                var opt = Stat.STAT_REGISTRY.getFlatMap().values().stream().filter(stat -> {
                    if (stat.metaData() instanceof AttributeStatData attributeStatData) {
                        return attributeStatData.attribute().equals(entityAttribute);
                    }
                    return false;
                }).findFirst();
                if (opt.isEmpty()) {
                    ResourceLocation id = Miapi.id("runtime_generated." + entityAttribute.getDescriptionId());
                    Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entityAttribute);
                    AttributeStatBuilder builder = new AttributeStatBuilder(id, StatGroups.MISC, holder);
                    builder.setDefault(entityAttribute.getDefaultValue())
                            .setInverse(((AttributeAccessor) entityAttribute).getMiapiSentiment().equals(Attribute.Sentiment.NEGATIVE))
                            .setDescription(Component.translatable(entityAttribute.getDescriptionId()))
                            .setName(Component.translatable(entityAttribute.getDescriptionId()))
                            .setTemporary(true)
                            .setMin(Math.max(-2048, entityAttribute.sanitizeValue(Double.MIN_VALUE)))
                            .setMax(Math.min(2048, entityAttribute.sanitizeValue(Double.MAX_VALUE)))
                            .register();
                }
            }
        }));
    }
}
