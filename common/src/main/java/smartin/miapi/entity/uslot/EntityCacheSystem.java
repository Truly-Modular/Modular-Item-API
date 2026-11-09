package smartin.miapi.entity.uslot;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntityCacheSystem {
    private static Codec<EntityCache> CODEC = new Codec<EntityCache>() {
        @Override
        public <T> DataResult<Pair<EntityCache, T>> decode(DynamicOps<T> ops, T input) {
            return null;
        }

        @Override
        public <T> DataResult<T> encode(EntityCache input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };
    private static final FacetKey<SimpleEntityFacet<EntityCache>> CACHE = SimpleEntityFacet
            .createSimple(Miapi.id("entity_slot_data"), CODEC)
            .setSaveCondition((e) -> false)
            .syncToClientsOnSet(false)
            .build(new EntityCache());

    static {
        MiapiEvents.LIVING_ENTITY_TICK_END.register(new MiapiEvents.LivingEntityTickEvent() {
            @Override
            public EventResult tick(LivingEntity entity) {
                CACHE.getOptional(entity)
                        .ifPresent(facet -> facet.get().tick());
                return EventResult.pass();
            }
        });
    }

    static class EntityCache {
        Map<ResourceLocation, CacheData<?>> cache = new HashMap<>();

        public void update(UEquipmentSlot changed, UEquipmentSlot.EquipedItem stack) {
            cache.forEach((id, data) -> data.update(changed, stack));
        }

        public void tick() {
            cache.forEach((id, data) -> data.tick());
        }
    }

    public static class EquipmentEntityCache<T> {
        ResourceLocation id;
        boolean updateOnHandSlots = false;
        boolean updateOnArmorSlots = false;
        boolean updateOnInventorySlots = false;
        boolean updateOnBackpackSlots = false;
        Function<UEquipmentSlot.EquipedItem, @Nullable T> valueGetter;

        public EquipmentEntityCache(ResourceLocation id, Function<UEquipmentSlot.EquipedItem, @Nullable T> valueGetter) {
            this.id = id;
            ReloadEvents.START.subscribe((isClient, registryAccess, worker) -> {
                reset();
            });
            this.valueGetter = valueGetter;
        }

        public Map<UEquipmentSlot.EquipedItem, T> getData(LivingEntity livingEntity) {
            EntityCache cache = CACHE.get(livingEntity).get();
            CacheData<T> data = (CacheData<T>) cache.cache.computeIfAbsent(id, (key) -> new CacheData<>(valueGetter));
            return data.getFull();
        }

        private void reset() {
            updateOnHandSlots = false;
            updateOnArmorSlots = false;
            updateOnInventorySlots = false;
            updateOnBackpackSlots = false;
        }

        public void allowSlot(SlotSelection selection) {
            this.updateOnArmorSlots |=
                    selection.matches(new EquipmentSlotKey(EquipmentSlot.FEET))
                    || selection.matches(new EquipmentSlotKey(EquipmentSlot.LEGS))
                    || selection.matches(new EquipmentSlotKey(EquipmentSlot.CHEST))
                    || selection.matches(new EquipmentSlotKey(EquipmentSlot.HEAD));

            this.updateOnHandSlots |=
                    selection.matches(new EquipmentSlotKey(EquipmentSlot.MAINHAND))
                    || selection.matches(new EquipmentSlotKey(EquipmentSlot.OFFHAND));
        }

        public void allowSlot(UEquipmentSlot slot) {
            Predicate<EquipmentSlot> matches =
                    slot instanceof EquipmentSlotKey key
                            ? s -> s == key.equipmentSlot()
                            : slot instanceof EquipmentSlotGroupKey group
                            ? s -> group.equipmentSlot().test(s)
                            : s -> false;

            this.updateOnArmorSlots |=
                    matches.test(EquipmentSlot.FEET)
                    || matches.test(EquipmentSlot.LEGS)
                    || matches.test(EquipmentSlot.CHEST)
                    || matches.test(EquipmentSlot.HEAD);

            this.updateOnHandSlots |=
                    matches.test(EquipmentSlot.MAINHAND)
                    || matches.test(EquipmentSlot.OFFHAND);
        }
    }

    static class CacheData<T> {
        Map<UEquipmentSlot.EquipedItem, T> dataCache = new ConcurrentHashMap<>();
        Map<UEquipmentSlot, UEquipmentSlot.EquipedItem> queuedUpdates = new ConcurrentHashMap<>();
        Function<UEquipmentSlot.EquipedItem, @Nullable T> valueGetter;
        Predicate<UEquipmentSlot> updatePredicate = (slot) -> true;
        UpdateCycle updateCycle = UpdateCycle.INSTANT;

        public CacheData(Function<UEquipmentSlot.EquipedItem, @Nullable T> getter) {
            this.valueGetter = getter;
        }

        public void update(UEquipmentSlot uEquipmentSlot, UEquipmentSlot.EquipedItem itemStack) {
            if (!updatePredicate.test(uEquipmentSlot)) {
                return;
            }
            if (updateCycle == UpdateCycle.INSTANT) {
                updateValue(itemStack);
            } else {
                queuedUpdates.put(uEquipmentSlot, itemStack);
            }
        }

        public void tick() {
            if (updateCycle == UpdateCycle.TICK) {
                executeUpdates();
            }
        }

        private void executeUpdates() {
            queuedUpdates.forEach((slot, item) -> {
                updateValue(item);
            });
        }

        private void updateValue(UEquipmentSlot.EquipedItem item) {
            T value = valueGetter.apply(item);
            if (value != null) {
                dataCache.put(item, value);
            }
        }

        /**
         * this EXPOSES the raw inner map.
         * this is done for performance reasons, but use with care since the map is concurrent!
         */
        public Map<UEquipmentSlot.EquipedItem, T> getFull() {
            executeUpdates();
            return dataCache;
        }
    }

    enum UpdateCycle {
        INSTANT,
        TICK,
        LAZY
    }
}