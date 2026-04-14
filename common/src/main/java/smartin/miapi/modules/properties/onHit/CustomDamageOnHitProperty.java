package smartin.miapi.modules.properties.onHit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.JsonStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.entity.EntityDamageSystem;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;

public class CustomDamageOnHitProperty extends CodecProperty<Map<ResourceLocation, CustomDamageOnHitProperty.CustomDamageData>> {
    public static ResourceLocation KEY = Miapi.id("custom_damage");
    public static final Codec<Map<ResourceLocation, CustomDamageData>> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, CustomDamageData.CODEC);
    public static CustomDamageOnHitProperty property = new CustomDamageOnHitProperty();

    private CustomDamageOnHitProperty() {
        super(CODEC);
        MiapiEvents.LIVING_ENTITY_TICK_END.register(entity -> {
            FACET.getOptional(entity).ifPresent(data -> Objects.requireNonNull(data.get()).tickDown());
            return EventResult.pass();
        });
        if (Platform.getEnv() == EnvType.CLIENT) {
            setupStatDisplayClient();
        }

        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, tooltipType) -> getData(itemStack).ifPresent((map) -> map.forEach((id, data) -> {
            data.tooltip.ifPresent((c -> {
                tooltip.add(Component.translatableWithFallback(c, c,
                        data.amount.getValue(),
                        data.defenderCooldown.getValue(),
                        data.attackerCooldown.getValue()));
            }));
        })));
        EntityDamageSystem.DAMAGE_EVENT.register((defender, originalSource, originalAmount, strength, applyCustomEffect) -> {
            if (originalSource.getEntity() == null) {
                return EventResult.pass();
            }
            boolean isRanged = !originalSource.getEntity().equals(originalSource.getDirectEntity());
            if (originalSource.getEntity() instanceof LivingEntity attacker) {
                EntityDamageSystem.getCausingItemStackAndArmorOfAttacker(originalSource).forEach(itemStack -> {
                    getData(itemStack).ifPresent(map -> {
                        map.forEach((id, data) -> {
                            if (data.type.isEmpty()) {
                                Miapi.LOGGER.warn("Damage Type is not set correctly for custom_damage {}", id);
                                return;
                            }
                            ResourceKey<DamageType> type = ResourceKey.create(Registries.DAMAGE_TYPE, data.type.get());
                            if (getCD(attacker, id, true) > 0) {
                                return;
                            }
                            if (getCD(defender, id, false) > 0) {
                                return;
                            }
                            if (isRanged && data.onRange.orElse(true) ||
                                data.onMelee.orElse(true)) {
                                applyCustomEffect.accept(id, () -> {
                                    float damage = (float) data.amount.getValue();
                                    if (data.respectAttackCooldown.orElse(true)) {
                                        damage = damage * strength;
                                    }
                                    defender.hurt(attacker.damageSources().source(type), damage);
                                    setCD(attacker, id, true, (int) data.attackerCooldown.getValue());
                                    setCD(defender, id, false, (int) data.defenderCooldown.getValue());
                                });
                            }
                        });
                    });
                });
            }
            return EventResult.pass();
        });
    }

    @Environment(EnvType.CLIENT)
    private void setupStatDisplayClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> combined = new ArrayList<>();

                Map<ResourceLocation, CustomDamageData> combinedMap = new LinkedHashMap<>();
                getData(original).ifPresent(combinedMap::putAll);
                getData(compareTo).ifPresent(combinedMap::putAll);

                combinedMap.forEach((id, data) -> {
                    JsonStatDisplay display = new JsonStatDisplay(
                            // header
                            (stack) -> data.header
                                    .map(Component::translatable)
                                    .orElse(Component.literal(id.toString())),

                            // description WITH injected values
                            (stack) -> data.description
                                    .map(desc -> Component.translatableWithFallback(
                                            desc,
                                            desc,
                                            data.amount.getValue(),
                                            data.defenderCooldown.getValue(),
                                            data.attackerCooldown.getValue()
                                    ))
                                    .orElse(Component.empty()),

                            // value reader
                            new SingleStatDisplayDouble.StatReaderHelper() {
                                @Override
                                public double getValue(ItemStack stack) {
                                    return data.amount.getValue();
                                }

                                @Override
                                public boolean hasValue(ItemStack stack) {
                                    return data.amount.getValue() != 0;
                                }
                            },

                            // min / max (adjust if needed)
                            0,
                            data.amount.getValue()
                    ) {
                        @Override
                        public DoubleOperationResolvable getResolvable(ItemStack stack) {
                            return data.amount;
                        }
                    };

                    combined.add((T) display);
                });

                return combined;
            }
        });
    }

    public static FacetKey<SimpleEntityFacet<CooldownData>> FACET =
            SimpleEntityFacet.createSimple(Miapi.id("custom_damage_cd"), CooldownData.CODEC)
                    .syncToClientsOnSet(false)
                    .setDefaultValue(new CooldownData())
                    .build();

    private static int getCD(LivingEntity e, ResourceLocation id, boolean attacker) {
        return FACET.getOptional(e).map(facet -> {
            CooldownData data = facet.get();
            if (data == null) {
                return 0;
            }
            return data.get(id, attacker);
        }).orElse(0);
    }

    private static void setCD(LivingEntity e, ResourceLocation id, boolean attacker, int value) {
        FACET.getOptional(e).ifPresent(facet -> {
            CooldownData data = facet.get();
            if (data == null) {
                data = new CooldownData();
                facet.set(data);
            }
            data.set(id, attacker, value);
        });
    }


    @Override
    public Map<ResourceLocation, CustomDamageData> merge(Map<ResourceLocation, CustomDamageData> left,
                                                         Map<ResourceLocation, CustomDamageData> right,
                                                         MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (k, l, r) -> MergeAble.autoMerge(l, r, mergeType, CustomDamageData.class));
    }

    @Override
    public Map<ResourceLocation, CustomDamageData> initialize(Map<ResourceLocation, CustomDamageData> property,
                                                              ModuleInstance context) {
        Map<ResourceLocation, CustomDamageData> result = new LinkedHashMap<>();

        property.forEach((key, value) -> {
            CustomDamageData damageData = InitializeAble.autoInitialize(value, context, CustomDamageData.class);
            result.put(key, damageData);
        });

        return result;
    }

    public static class CustomDamageData {
        public static MapCodec<CustomDamageData> MAPCODEC = AutoCodec.of(CustomDamageData.class);
        public static Codec<CustomDamageData> CODEC = MAPCODEC.codec();

        public Optional<ResourceLocation> type = Optional.empty();
        public @AutoCodec.Name("on_ranged_attack") Optional<Boolean> onRange = Optional.empty();
        public @AutoCodec.Name("on_melee_attack") Optional<Boolean> onMelee = Optional.empty();
        public @AutoCodec.Name("respect_attack_cooldown") Optional<Boolean> respectAttackCooldown = Optional.empty();
        public @CodecBehavior.Optional
        @AutoCodec.Name("defender_cooldown") DoubleOperationResolvable defenderCooldown = new DoubleOperationResolvable(0);
        public @CodecBehavior.Optional
        @AutoCodec.Name("attacker_cooldown") DoubleOperationResolvable attackerCooldown = new DoubleOperationResolvable(0);
        public @CodecBehavior.Optional DoubleOperationResolvable amount = new DoubleOperationResolvable(0);
        public Optional<String> tooltip = Optional.empty();
        public Optional<String> header = Optional.empty();
        public Optional<String> description = Optional.empty();
    }

    public static class CooldownData {
        //eh custom codec cause i need mutable maps.
        public static final Codec<CooldownData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("attacker").forGetter(d -> d.attacker),
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("defender").forGetter(d -> d.defender)
        ).apply(instance, (attacker, defender) -> {
            CooldownData data = new CooldownData();
            data.attacker.putAll(attacker);
            data.defender.putAll(defender);
            return data;
        }));
        private final Map<ResourceLocation, Integer> attacker = new HashMap<>();
        private final Map<ResourceLocation, Integer> defender = new HashMap<>();

        public int get(ResourceLocation id, boolean isAttacker) {
            return (isAttacker ? attacker : defender).getOrDefault(id, 0);
        }

        public void set(ResourceLocation id, boolean isAttacker, int value) {
            (isAttacker ? attacker : defender).put(id, value);
        }

        public void tickDown() {
            tickMap(attacker);
            tickMap(defender);
        }

        private void tickMap(Map<ResourceLocation, Integer> map) {
            map.replaceAll((id, v) -> Math.max(0, v - 1));
            map.values().removeIf(v -> v == 0);
        }
    }
}