package smartin.miapi.entity;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.CodecEntityFacet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HurtTimerFacet implements CodecEntityFacet<Map<ResourceLocation, HurtTimerFacet.DamageSnapshot>> {
    private Map<ResourceLocation, DamageSnapshot> hurtTimers = new HashMap<>();
    public static final ResourceLocation facetIdentifier = Miapi.id("hurt_timer");
    public static final FacetKey<HurtTimerFacet> KEY = (FacetKey<HurtTimerFacet>) CodecEntityFacet.create(facetIdentifier, (HurtTimerFacet::new), HurtTimerFacet.class);
    public static Codec<Map<ResourceLocation, HurtTimerFacet.DamageSnapshot>> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, DamageSnapshot.CODEC);


    public HurtTimerFacet(Entity entity) {
    }

    public DamageSnapshot get(ResourceLocation id) {
        return hurtTimers.getOrDefault(id, new DamageSnapshot());
    }


    public void put(ResourceLocation id, DamageSnapshot snapshot) {
        hurtTimers.put(id, snapshot);
    }


    public void remove(ResourceLocation id) {
        hurtTimers.remove(id);
    }


    public void tick() {
        if (hurtTimers.isEmpty()) return;
        Iterator<Map.Entry<ResourceLocation, DamageSnapshot>> iterator =
                hurtTimers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, DamageSnapshot> entry = iterator.next();
            DamageSnapshot snapshot = entry.getValue();
            //snapshot.hurtTime++;
            snapshot.hurtDuration--;
            if (snapshot.hurtDuration <= 0) {
                iterator.remove();
            }
        }
    }

    @Override
    public Codec<Map<ResourceLocation, DamageSnapshot>> getCodec() {
        return CODEC;
    }

    @Override
    public Map<ResourceLocation, DamageSnapshot> get() {
        return hurtTimers;
    }

    @Override
    public void set(Map<ResourceLocation, DamageSnapshot> value) {
        this.hurtTimers = new HashMap<>(value);
    }

    @Override
    public FacetKey<? extends CodecEntityFacet<Map<ResourceLocation, DamageSnapshot>>> getKey() {
        return KEY;
    }


    public static class DamageSnapshot {
        public static Codec<DamageSnapshot> CODEC = AutoCodec.of(DamageSnapshot.class).codec();

        public int hurtTime = 0;
        public int hurtDuration = 0;
        public int invulnerableTime = 0;

        public void apply(LivingEntity livingEntity) {
            livingEntity.hurtTime = this.hurtTime;
            livingEntity.hurtDuration = this.hurtDuration;
            livingEntity.invulnerableTime = this.invulnerableTime;
        }

        public void read(LivingEntity livingEntity) {
            this.hurtTime = livingEntity.hurtTime;
            this.hurtDuration = livingEntity.hurtDuration;
            this.invulnerableTime = livingEntity.invulnerableTime;
        }
    }
}