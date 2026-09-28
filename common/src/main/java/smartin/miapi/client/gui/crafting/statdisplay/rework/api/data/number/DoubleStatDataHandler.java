package smartin.miapi.client.gui.crafting.statdisplay.rework.api.data.number;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.statdisplay.rework.StatTypes;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.Stat;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatDecoder;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatGroup;
import smartin.miapi.client.gui.crafting.statdisplay.rework.api.StatText;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class DoubleStatDataHandler
        implements StatDecoder<DoubleOperationResolvable, DoubleStatData> {

    public static final ResourceLocation TYPE_ID =
            ResourceLocation.fromNamespaceAndPath("miapi", "number");

    public static final MapCodec<DoubleStatDataHandler> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("property")
                            .forGetter(data -> data.property),

                    ResourceLocation.CODEC
                            .fieldOf("group")
                            .forGetter(data -> data.group),

                    com.mojang.serialization.Codec.DOUBLE
                            .optionalFieldOf("min", 0D)
                            .forGetter(data -> data.min),

                    com.mojang.serialization.Codec.DOUBLE
                            .optionalFieldOf("max", 100D)
                            .forGetter(data -> data.max),

                    com.mojang.serialization.Codec.DOUBLE
                            .optionalFieldOf("default", 0D)
                            .forGetter(data -> data.defaultValue),

                    com.mojang.serialization.Codec.BOOL
                            .optionalFieldOf("inverse", false)
                            .forGetter(data -> data.inverse),

                    com.mojang.serialization.Codec.DOUBLE
                            .optionalFieldOf("priority", 0D)
                            .forGetter(data -> data.priority),

                    com.mojang.serialization.Codec.STRING
                            .optionalFieldOf("format", "##.##")
                            .forGetter(data -> data.format),

                    ComponentSerialization.CODEC
                            .optionalFieldOf("name")
                            .forGetter(data -> data.name),

                    ComponentSerialization.CODEC
                            .optionalFieldOf("description")
                            .forGetter(data -> data.description)
            ).apply(instance, DoubleStatDataHandler::new));

    public static final Type<
            DoubleOperationResolvable,
            DoubleStatData
            > TYPE = new Type<>(TYPE_ID, CODEC);

    private final ResourceLocation property;
    private final ResourceLocation group;

    private final double min;
    private final double max;
    private final double defaultValue;
    private final boolean inverse;
    private final double priority;
    private final String format;

    private final Optional<Component> name;
    private final Optional<Component> description;

    public DoubleStatDataHandler(
            ResourceLocation property,
            ResourceLocation group,
            double min,
            double max,
            double defaultValue,
            boolean inverse,
            double priority,
            String format,
            Optional<Component> name,
            Optional<Component> description
    ) {
        this.property = property;
        this.group = group;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.inverse = inverse;
        this.priority = priority;
        this.format = format;
        this.name = name;
        this.description = description;
    }

    @Override
    public Stat<DoubleOperationResolvable, DoubleStatData> getStat(
            ResourceLocation datapackLocation
    ) {
        var propertyEntry =
                RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.get(property);

        if (!(propertyEntry instanceof DoubleProperty resolvedProperty)) {
            throw new IllegalArgumentException(
                    "Unknown double property: " + property
            );
        }

        StatGroup statGroup = StatGroup.getOrRegister(group);

        DecimalFormat formatter = new DecimalFormat(
                format,
                DecimalFormatSymbols.getInstance(Locale.ROOT)
        );

        String langKey =
                Miapi.MOD_ID + ".stat." +
                datapackLocation.toLanguageKey();

        StatText<DoubleOperationResolvable, DoubleStatData> statName =
                name.map(component ->
                        (StatText<DoubleOperationResolvable, DoubleStatData>)
                                (value, data, context) -> component
                ).orElseGet(() ->
                        (value, data, context) ->
                                Component.translatable(
                                        langKey,
                                        new DoubleStatData.NumberWrapper(
                                                value == null
                                                        ? defaultValue
                                                        : data.getValue(),
                                                formatter
                                        )
                                )
                );

        StatText<DoubleOperationResolvable, DoubleStatData> statDescription =
                description.map(component ->
                        (StatText<DoubleOperationResolvable, DoubleStatData>)
                                (value, data, context) -> component
                ).orElseGet(() ->
                        (value, data, context) ->
                                Component.translatable(
                                        langKey + ".description",
                                        new DoubleStatData.NumberWrapper(
                                                value == null
                                                        ? defaultValue
                                                        : data.getValue(),
                                                formatter
                                        )
                                )
                );

        Function<ItemStack, DoubleOperationResolvable> getter =
                stack -> resolvedProperty.getData(stack).orElse(null);

        DoubleStatData metadata = new DoubleStatData(
                getter,
                min,
                max,
                defaultValue,
                inverse,
                priority
        );

        return new Stat<>(
                datapackLocation,
                statName,
                statDescription,
                StatTypes.DOUBLE_RESOLVEABLE_TYPE,
                statGroup,
                metadata
        );
    }

    @Override
    public Type<
            DoubleOperationResolvable,
            DoubleStatData
            > getType() {
        return TYPE;
    }

    public static void registerType() {
        StatDecoder.REGISTRY.register(TYPE_ID, TYPE);
    }
}