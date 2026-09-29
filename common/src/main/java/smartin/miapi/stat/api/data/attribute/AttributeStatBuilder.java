package smartin.miapi.stat.api.data.attribute;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

public class AttributeStatBuilder {

    private final ResourceLocation id;
    private final StatGroup group;
    private final Holder<Attribute> attribute;

    private EquipmentSlot slot = null;
    private double defaultValue;
    private double fallbackValue;
    private StatText<Double, AttributeStatData> name;
    private StatText<Double, AttributeStatData> description =
            (item, data, meta) -> Component.empty();

    private double min;
    private boolean temp = false;
    private double max;
    private double priority = 0;
    private DecimalFormat formatter = createFormatter("##.##");
    private boolean inverse = false;

    public AttributeStatBuilder(
            ResourceLocation id,
            StatGroup group,
            Holder<Attribute> attribute
    ) {
        this.id = id;
        this.group = group;
        this.attribute = attribute;

        double attributeDefault = attribute.value().getDefaultValue();

        this.defaultValue = attributeDefault;
        this.fallbackValue = attributeDefault;

        this.min = Math.max(
                -2048,
                attribute.value().sanitizeValue(Double.MIN_VALUE)
        );

        this.max = Math.min(
                2048,
                attribute.value().sanitizeValue(Double.MAX_VALUE)
        );

        this.name = (item, data, meta) ->
                Component.translatable(attribute.value().getDescriptionId());
    }

    public AttributeStatBuilder setMin(double min) {
        this.min = min;
        return this;
    }

    public AttributeStatBuilder setMax(double max) {
        this.max = max;
        return this;
    }

    public AttributeStatBuilder setDefault(double defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public AttributeStatBuilder setTemporary(boolean isTemp) {
        this.temp = isTemp;
        return this;
    }

    public AttributeStatBuilder setFallback(double fallbackValue) {
        this.fallbackValue = fallbackValue;
        return this;
    }

    public AttributeStatBuilder setSlot(EquipmentSlot slot) {
        this.slot = slot;
        return this;
    }

    public AttributeStatBuilder setName(StatText<Double, AttributeStatData> name) {
        this.name = name;
        return this;
    }

    public AttributeStatBuilder setName(Component name) {
        this.name = (item, data, meta) -> name;
        return this;
    }

    public AttributeStatBuilder setDescription(
            StatText<Double, AttributeStatData> description
    ) {
        this.description = description;
        return this;
    }

    public AttributeStatBuilder setDescription(Component description) {
        this.description = (item, data, meta) -> description;
        return this;
    }

    public AttributeStatBuilder setFormat(String format) {
        this.formatter = createFormatter(format);
        return this;
    }


    /**
     * Sets the priority used when rendering this stat. Lower values are rendered earlier.
     */
    public AttributeStatBuilder setPriority(double priority) {
        this.priority = priority;
        return this;
    }

    public AttributeStatBuilder setFormatter(DecimalFormat formatter) {
        this.formatter = formatter;
        return this;
    }

    public AttributeStatBuilder setInverse(boolean inverse) {
        this.inverse = inverse;
        return this;
    }

    public AttributeStatBuilder setTranslationKey(String key) {
        this.name = (item, data, meta) ->
                Component.translatable("miapi.stat." + key);

        this.description = (item, data, meta) ->
                Component.translatable("miapi.stat." + key + ".description");

        return this;
    }

    public Stat<Double, AttributeStatData>[] register() {
        Stat<Double, AttributeStatData> additive = create(
                id.withPrefix("attribute." + group.getID().toLanguageKey() + "."),
                ADD_VALUE,
                min,
                max,
                defaultValue,
                priority
        );

        Stat<Double, AttributeStatData> multipliedBase = create(
                id.withPrefix("attribute." + group.getID().toLanguageKey() + ".")
                        .withSuffix("_percent"),
                ADD_MULTIPLIED_BASE,
                0,
                100,
                defaultValue,
                priority
        );

        Stat<Double, AttributeStatData> multipliedTotal = create(
                id.withPrefix("attribute." + group.getID().toLanguageKey() + ".")
                        .withSuffix("_percent_total"),
                ADD_MULTIPLIED_TOTAL,
                0,
                100,
                defaultValue,
                priority
        );

        return new Stat[]{
                additive,
                multipliedBase,
                multipliedTotal
        };
    }

    private Stat<Double, AttributeStatData> create(
            ResourceLocation statId,
            AttributeModifier.Operation operation,
            double min,
            double max,
            double defaultValue,
            double priority
    ) {
        AttributeStatData metaData = new AttributeStatData(
                attribute,
                slot,
                operation,
                formatter,
                defaultValue,
                min,
                max,
                fallbackValue,
                priority
        );
        Stat<Double, AttributeStatData> stat = new Stat<>(
                statId,
                name,
                description,
                StatTypes.ATTRIBUTE_TYPE,
                group,
                metaData
        );
        if (temp) {
            Stat.STAT_REGISTRY.registerTemporary(statId, stat);
        } else {
            Stat.STAT_REGISTRY.register(statId, stat);
        }
        return stat;
    }

    private static DecimalFormat createFormatter(String format) {
        DecimalFormat formatter = new DecimalFormat(format);
        formatter.setDecimalFormatSymbols(
                DecimalFormatSymbols.getInstance(Locale.ROOT)
        );
        return formatter;
    }
}