package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import dev.architectury.event.EventResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.entity.EntityHelper;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

public class WalkingSoundProperty extends CodecProperty<List<WalkingSoundProperty.SoundData>> {
    private static final SoundInfo DEFAULT_START_SOUND = new SoundInfo(1.0f, 0.2f, 1.0f);
    private static final SoundInfo DEFAULT_END_SOUND = new SoundInfo(1.0f, 1.0f, 2.0f);
    public static final Codec<SoundData> SOUND_DATA_CODEC = AutoCodec.of(SoundData.class).codec();
    public static final Codec<List<SoundData>> CODEC = Codec.list(SOUND_DATA_CODEC);
    private static final WalkingSoundProperty PROPERTY = new WalkingSoundProperty();
    private static final FacetKey<SimpleEntityFacet<Map<String, Long>>> WALKING_SOUND_TIMES = SimpleEntityFacet.createSimple(
                    Miapi.id("walking_sound_times"),
                    (Codec<Map<String, Long>>) null)
            .syncToClientsOnSet(false)
            .setSaveCondition(a -> false)
            .build(new HashMap<>());

    private WalkingSoundProperty() {
        this(CODEC);
    }

    protected WalkingSoundProperty(Codec<List<SoundData>> codec) {
        super(codec);
        MiapiEvents.LIVING_ENTITY_TICK_END.register(this::tick);
    }

    @Override
    public List<SoundData> merge(List<SoundData> left, List<SoundData> right, MergeType mergeType) {
        return List.copyOf(MergeAble.mergeList(left, right, mergeType));
    }

    private EventResult tick(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            return EventResult.pass();
        }

        var timerFacet = WALKING_SOUND_TIMES.get(entity);
        if (timerFacet == null) {
            return EventResult.pass();
        }

        Map<String, Long> lastSoundTimes = timerFacet.get();

        if (lastSoundTimes == null) {
            lastSoundTimes = new HashMap<>();
            timerFacet.set(lastSoundTimes);
        }

        double speed = entity.getDeltaMovement().horizontalDistance();
        long now = entity.level().getGameTime();

        boolean canPlay = entity.onGround() && !entity.isPassenger();

        Set<String> activeSchedules = new HashSet<>();

        for (ItemStack stack : EntityHelper.getAllEquipedItems(entity)) {
            List<SoundData> soundDataList = getData(stack).orElse(List.of());

            for (SoundData data : soundDataList) {
                if (data == null) {
                    continue;
                }

                String timingKey = data.timingKey();
                activeSchedules.add(timingKey);

                if (!canPlay || speed < data.minSpeed() || speed <= 0.0) {
                    continue;
                }

                float progress = Mth.clamp((float) ((speed - data.minSpeed()) / (data.maxSpeed() - data.minSpeed())), 0.0f, 1.0f);

                SoundInfo start = data.startingSound();
                SoundInfo end = data.endSound();

                float pitch = Mth.lerp(progress, start.pitch(), end.pitch());
                float volume = Mth.lerp(progress, start.amplifier(), end.amplifier());

                float frequency = Mth.lerp(progress, start.frequency(), end.frequency()) * data.frequency();

                if (!Float.isFinite(frequency) || frequency <= 0.0f) {
                    continue;
                }

                double intervalTicks = 20.0 / frequency;

                Long lastPlayed = lastSoundTimes.get(timingKey);

                if (lastPlayed != null && now >= lastPlayed && now - lastPlayed < intervalTicks) {
                    continue;
                }

                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(data.soundId()).orElse(null);

                if (soundEvent == null) {
                    continue;
                }

                SoundSource source = entity instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL;

                entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), soundEvent, source, volume, pitch, false);

                lastSoundTimes.put(timingKey, now);
            }
        }

        lastSoundTimes.keySet().retainAll(activeSchedules);

        return EventResult.pass();
    }

    private static float nonNegativeOrDefault(Float value, float fallback) {
        if (value == null || !Float.isFinite(value) || value < 0.0f) {
            return fallback;
        }

        return value;
    }

    private static float positiveOrDefault(Float value, float fallback) {
        if (value == null || !Float.isFinite(value) || value <= 0.0f) {
            return fallback;
        }

        return value;
    }

    @CodecBehavior.Override(auto = true)
    public record SoundData(ResourceLocation soundId,
                            @AutoCodec.Name("startingSpeed") @CodecBehavior.Optional Float minSpeed,
                            @CodecBehavior.Optional SoundInfo startingSound,
                            @AutoCodec.Name("topSpeed") @CodecBehavior.Optional Float maxSpeed,
                            @CodecBehavior.Optional Float frequency, @CodecBehavior.Optional SoundInfo endSound) {
        public SoundData {
            Objects.requireNonNull(soundId, "soundId");
            minSpeed = nonNegativeOrDefault(minSpeed, 0.02f);
            maxSpeed = positiveOrDefault(maxSpeed, 0.2f);
            if (maxSpeed <= minSpeed) {
                maxSpeed = minSpeed + 0.001f;
            }
            frequency = nonNegativeOrDefault(frequency, 1.0f);
            startingSound = startingSound == null ? DEFAULT_START_SOUND : startingSound;
            endSound = endSound == null ? DEFAULT_END_SOUND : endSound;
        }

        private String timingKey() {
            return toString();
        }
    }

    @CodecBehavior.Override(auto = true)
    @AutoCodec.Settings(defaultOptionalBehavior = @CodecBehavior.Optional(value = true, encodeToNull = false))
    public record SoundInfo(Float pitch, Float amplifier, Float frequency) {
        public SoundInfo {
            // Neutral pitch, default volume, default rate.
            pitch = positiveOrDefault(pitch, 1.0f);
            amplifier = nonNegativeOrDefault(amplifier, 0.2f);
            frequency = nonNegativeOrDefault(frequency, 1.0f);
        }
    }
}