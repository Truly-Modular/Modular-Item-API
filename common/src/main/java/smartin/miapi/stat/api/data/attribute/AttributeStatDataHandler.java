package smartin.miapi.stat.api.data.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.StatDecoder;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

public class AttributeStatDataHandler
        implements StatDecoder<Double, AttributeStatData> {

    public static final ResourceLocation TYPE_ID =
            ResourceLocation.fromNamespaceAndPath("miapi", "attribute");

    public static final MapCodec<AttributeStatDataHandler> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.ATTRIBUTE.holderByNameCodec()
                            .fieldOf("attribute")
                            .forGetter(data -> data.attribute),

                    ResourceLocation.CODEC
                            .fieldOf("group")
                            .forGetter(data -> data.group),

                    AttributeModifier.Operation.CODEC
                            .optionalFieldOf(
                                    "operation",
                                    AttributeModifier.Operation.ADD_VALUE
                            )
                            .forGetter(data -> data.operation),

                    EquipmentSlot.CODEC
                            .optionalFieldOf("slot")
                            .forGetter(data -> data.slot),

                    Codec.DOUBLE.optionalFieldOf("default")
                            .forGetter(data -> data.defaultValue),

                    Codec.DOUBLE.optionalFieldOf("fallback")
                            .forGetter(data -> data.fallbackValue),

                    Codec.DOUBLE.optionalFieldOf("min")
                            .forGetter(data -> data.min),

                    Codec.DOUBLE.optionalFieldOf("max")
                            .forGetter(data -> data.max),

                    Codec.DOUBLE.optionalFieldOf("priority", 0D)
                            .forGetter(data -> data.priority),

                    Codec.STRING.optionalFieldOf("format")
                            .forGetter(data -> data.format),

                    Codec.BOOL.optionalFieldOf("inverse", false)
                            .forGetter(data -> data.inverse),

                    ComponentSerialization.CODEC.optionalFieldOf("name")
                            .forGetter(data -> data.name),

                    ComponentSerialization.CODEC.optionalFieldOf("description")
                            .forGetter(data -> data.description)
            ).apply(instance, AttributeStatDataHandler::new));

    public static final Type<Double, AttributeStatData> TYPE =
            new Type<>(TYPE_ID, CODEC);

    private final Holder<Attribute> attribute;
    private final ResourceLocation group;
    private final AttributeModifier.Operation operation;

    private final Optional<EquipmentSlot> slot;
    private final Optional<Double> defaultValue;
    private final Optional<Double> fallbackValue;
    private final Optional<Double> min;
    private final Optional<Double> max;
    private final double priority;
    private final Optional<String> format;
    private final boolean inverse;

    private final Optional<Component> name;
    private final Optional<Component> description;

    public AttributeStatDataHandler(
            Holder<Attribute> attribute,
            ResourceLocation group,
            AttributeModifier.Operation operation,
            Optional<EquipmentSlot> slot,
            Optional<Double> defaultValue,
            Optional<Double> fallbackValue,
            Optional<Double> min,
            Optional<Double> max,
            double priority,
            Optional<String> format,
            boolean inverse,
            Optional<Component> name,
            Optional<Component> description
    ) {
        this.attribute = attribute;
        this.group = group;
        this.operation = operation;
        this.slot = slot;
        this.defaultValue = defaultValue;
        this.fallbackValue = fallbackValue;
        this.min = min;
        this.max = max;
        this.priority = priority;
        this.format = format;
        this.inverse = inverse;
        this.name = name;
        this.description = description;
    }

    @Override
    public Stat<Double, AttributeStatData> getStat(
            ResourceLocation datapackLocation
    ) {
        double attributeDefault = attribute.value().getDefaultValue();

        double resolvedDefault = defaultValue.orElse(attributeDefault);
        double resolvedFallback = fallbackValue.orElse(attributeDefault);

        double attributeMin = Math.max(
                -2048,
                attribute.value().sanitizeValue(Double.MIN_VALUE)
        );

        double attributeMax = Math.min(
                2048,
                attribute.value().sanitizeValue(Double.MAX_VALUE)
        );

        double resolvedMin = min.orElse(
                operation == AttributeModifier.Operation.ADD_VALUE
                        ? attributeMin
                        : 0
        );

        double resolvedMax = max.orElse(
                operation == AttributeModifier.Operation.ADD_VALUE
                        ? attributeMax
                        : 100
        );

        AttributeStatData metadata = new AttributeStatData(
                attribute,
                slot.orElse(null),
                operation,
                new DecimalFormat(format.orElse("##.##")),
                resolvedDefault,
                resolvedMin,
                resolvedMax,
                resolvedFallback,
                priority
        );

        String langKey =
                "miapi.stat." + datapackLocation.toLanguageKey();

        StatText<Double, AttributeStatData> statName =
                name.<StatText<Double, AttributeStatData>>map(component ->
                        (value, data, context) -> component
                ).orElseGet(() ->
                        (value, data, context) ->
                                Component.translatable(
                                        langKey,
                                        value,
                                        operation.getSerializedName()
                                )
                );

        StatText<Double, AttributeStatData> statDescription =
                description.<StatText<Double, AttributeStatData>>map(component ->
                        (value, data, context) -> component
                ).orElseGet(() ->
                        (value, data, context) ->
                                Component.translatable(
                                        langKey + ".description",
                                        value,
                                        operation.getSerializedName()
                                )
                );

        /*
         * Currently retained from the original implementation.
         * Use resolvedMin/resolvedMax when Stat supports value bounds.
         */
        DecimalFormat formatter = new DecimalFormat(
                format.orElse("##.##"),
                DecimalFormatSymbols.getInstance(Locale.ROOT)
        );

        return new Stat<>(
                datapackLocation,
                statName,
                statDescription,
                StatTypes.ATTRIBUTE_TYPE,
                StatGroup.getOrRegister(group),
                metadata
        );
    }

    @Override
    public Type<Double, AttributeStatData> getType() {
        return TYPE;
    }

    public static void registerType() {
        StatDecoder.REGISTRY.register(TYPE_ID, TYPE);
    }
}