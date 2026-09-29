package smartin.miapi.modules.properties.onHit.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Helper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.DoubleResolvableStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.stat.StatGroups;
import smartin.miapi.stat.api.StatAggregator;
import smartin.miapi.stat.api.StatText;
import smartin.miapi.stat.api.StatValue;
import smartin.miapi.stat.api.data.number.DoubleStatData;

import java.util.*;

/**
 * This is a generic system to add an entity based strength
 */
public abstract class GenericEntityStrengthProperty extends CodecProperty<Map<ResourceLocation, GenericEntityStrengthProperty.EntityContext>> {


    protected GenericEntityStrengthProperty() {
        super(Codec.unboundedMap(Miapi.ID_CODEC, EntityContext.CODEC));
        if (Platform.getEnv() == EnvType.CLIENT) {
            setupToolTip();
        }
        StatAggregator.register(StatGroups.ON_HIT, (group, helper, baseItem, compareItem) -> {
            Map<ResourceLocation, EntityContext> map =
                    new HashMap<>(getData(baseItem).orElse(Map.of()));
            map.putAll(getData(compareItem).orElse(Map.of()));

            map.forEach((id, edit) -> {
                EntityType<?> exampleType = edit.firstType();
                if (exampleType == null) {
                    return;
                }
                helper.addStat(
                        new StatValue.StatWithValues<>(
                                buildStat(id, edit, exampleType)
                                        .setMin(0)
                                        .setMax(8)
                                        .build(),
                                baseItem,
                                compareItem
                        )
                );
            });
        });
    }

    private DoubleStatData.DoubleStatBuilder buildStat(ResourceLocation id, EntityContext edit, EntityType<?> exampleType) {
        return DoubleStatData.getBuilder(
                Miapi.id("runtime_entity_strength_" + id.getNamespace() + "_" + id.getPath()),
                (item, resolvable, statData) ->
                        edit.name()
                                .orElse(getFallbackName(exampleType)),
                buiildDescription(id, edit, exampleType),
                StatGroups.ON_HIT,
                item -> getData(item)
                        .filter(a -> a.containsKey(id))
                        .map(a -> a.get(id).strength())
                        .orElse(new DoubleOperationResolvable(0))
        );
    }

    private @NotNull StatText<DoubleOperationResolvable, DoubleStatData> buiildDescription(ResourceLocation id, EntityContext edit, EntityType<?> exampleType) {
        return (item, resolvable, statData) -> {
            MutableComponent component = Component.empty();
            component
                    .append(edit.name().orElse(getFallbackName(exampleType)))
                    .append("\n")
                    .append(getBaseDescription(
                            getData(item)
                                    .filter(a -> a.containsKey(id))
                                    .map(a -> a.get(id).strength().getValue())
                                    .orElse(0.0)
                    ))
                    .append("\n")
                    .append(Component.translatable("miapi.property.entity.source"))
                    .append("\n");

            edit.entities().forEach(context -> {
                if (context instanceof HolderSet.Named<EntityType<?>> named) {
                    component.append(Helper.getTranslation(named.key()))
                            .append("\n");
                } else if (context instanceof HolderSet.ListBacked<EntityType<?>> listBacked) {
                    listBacked.forEach(type ->
                            component.append(type.value().getDescription())
                                    .append("\n")
                    );
                }
            });

            return component;
        };
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("unchecked")
    public void setupToolTip() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();
                Map<ResourceLocation, GenericEntityStrengthProperty.EntityContext> map = new HashMap<>(getData(original).orElse(Map.of()));
                map.putAll(getData(compareTo).orElse(Map.of()));
                map.forEach((id, edit) -> {
                    EntityType<?> exampleType = edit.firstType();
                    if (exampleType != null) {
                        displays.add((T) DoubleResolvableStatDisplay
                                .builder((s) -> getData(s)
                                        .filter(a -> a.containsKey(id))
                                        .map(a -> a.get(id).strength()))
                                .setMax(8)
                                .setName(edit.name().orElse(getFallbackName(exampleType)))
                                .setHoverDescription(stack -> {
                                    MutableComponent component = Component.empty();
                                    component
                                            .append(edit.name().orElse(getFallbackName(exampleType)))
                                            .append("\n")
                                            .append(getBaseDescription(
                                                    getData(stack)
                                                            .filter(a -> a.containsKey(id))
                                                            .map(a -> a.get(id).strength().getValue()).orElse(0.0)))
                                            .append("\n")
                                            .append(Component.translatable("miapi.property.entity.source"))
                                            .append("\n");
                                    edit.entities().forEach(context -> {
                                        if (context instanceof HolderSet.Named<EntityType<?>> named) {
                                            component.append(Helper.getTranslation(named.key())).append("\n");
                                        } else if (context instanceof HolderSet.ListBacked<EntityType<?>> listBacked) {
                                            listBacked.forEach(type -> {
                                                component.append(type.value().getDescription()).append("\n");
                                            });
                                        }
                                    });
                                    return component;
                                })
                                .build());
                    }
                });
                return displays;
            }
        });
    }

    public abstract Component getFallbackName(EntityType<?> firstType);

    public abstract Component getBaseDescription(double strength);

    public double strengthForEntity(Holder<EntityType<?>> entity, ItemStack itemStack) {
        return getData(itemStack).map(a -> {
            double value = 0;
            for (EntityContext context : a.values()) {
                if (context.contains(entity)) {
                    value += context.strength().getValue();
                }
            }
            return value;
        }).orElse(0.0);
    }

    public double strengthForEntity(Holder<EntityType<?>> entity, Iterable<ItemStack> stacks) {
        double strength = 0;
        for (ItemStack itemStack : stacks) {
            strength += strengthForEntity(entity, itemStack);
        }
        return strength;
    }

    @Override
    public Map<ResourceLocation, EntityContext> merge(Map<ResourceLocation, EntityContext> left, Map<ResourceLocation, EntityContext> right, MergeType mergeType) {
        MergeAble.mergeMap(left, right, mergeType, (key, l, r) -> new EntityContext(
                MergeAble.mergeList(l.entities(), r.entities(), mergeType),
                r.strength().merge(l.strength(), mergeType),
                MergeAble.decideLeftRight(r.name(), l.name(), mergeType)));
        return Map.of();
    }

    @Override
    public Map<ResourceLocation, EntityContext> initialize(Map<ResourceLocation, EntityContext> property, ModuleInstance context) {
        Map<ResourceLocation, EntityContext> init = new HashMap<>();
        property.forEach((id, entityContext) -> {
            init.put(id, new EntityContext(entityContext.entities, entityContext.strength.initialize(context), entityContext.name));
        });
        return init;
    }

    public record EntityContext(List<HolderSet<EntityType<?>>> entities,
                                DoubleOperationResolvable strength, Optional<Component> name) {
        public static final Codec<EntityContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Miapi.toListOrSimple(RegistryCodecs
                        .homogeneousList(Registries.ENTITY_TYPE)).optionalFieldOf("entities", List.of()).forGetter(EntityContext::entities),
                DoubleOperationResolvable.CODEC.optionalFieldOf("strength", new DoubleOperationResolvable(0)).forGetter(EntityContext::strength),
                ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(EntityContext::name)
        ).apply(instance, EntityContext::new));

        public boolean contains(Holder<EntityType<?>> entity) {
            for (HolderSet<EntityType<?>> holders : entities()) {
                if (holders.contains(entity)) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        public EntityType<?> firstType() {
            for (HolderSet<EntityType<?>> holder : entities()) {
                if (holder instanceof HolderSet.ListBacked<EntityType<?>> listBacked) {
                    Optional<EntityType<?>> type = listBacked.stream().findFirst().map(Holder::value);
                    if (type.isPresent()) {
                        return type.get();
                    }
                }
            }
            return null;
        }
    }
}
