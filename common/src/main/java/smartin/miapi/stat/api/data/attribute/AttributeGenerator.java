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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class AttributeGenerator {
    private AttributeGenerator() {

    }

    public static void setup() {
        ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> BuiltInRegistries.ATTRIBUTE.forEach(entityAttribute -> {
            if (entityAttribute != null) {
                Set<Attribute> registeredAttributes =
                        Collections.newSetFromMap(new IdentityHashMap<>());
                for (Stat<?, ?> stat :
                        Stat.STAT_REGISTRY.getFlatMap().values()) {
                    if (stat.metaData() instanceof AttributeStatData attributeStatData) {
                        registeredAttributes.add(attributeStatData.attribute().value());
                    }
                }
                if (!registeredAttributes.contains(entityAttribute)) {
                    ResourceLocation id = Miapi.id("runtime_generated." + entityAttribute.getDescriptionId());
                    if (!Stat.STAT_REGISTRY.containsKey(id)) {
                        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entityAttribute);
                        AttributeStatBuilder builder = new AttributeStatBuilder(id, StatGroups.MISC, holder);
                        builder.setDefault(entityAttribute.getDefaultValue())
                                .setInverse(((AttributeAccessor) entityAttribute).getMiapiSentiment().equals(Attribute.Sentiment.NEGATIVE))
                                .setDescription(Component.translatable(entityAttribute.getDescriptionId()))
                                .setName(Component.translatable(entityAttribute.getDescriptionId()))
                                .setTemporary(true)
                                .setMin(Math.max(-2048, entityAttribute.sanitizeValue(-Double.MAX_VALUE)))
                                .setMax(Math.min(2048, entityAttribute.sanitizeValue(Double.MAX_VALUE)))
                                .register();
                    }
                }
            }
        }));
    }
}
