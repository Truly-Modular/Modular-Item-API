package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.AutoCodec;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

public class PlaySoundProperty extends CodecBasedProperty<List<PlaySoundProperty.Holder>> {
    public static final String KEY = "playSounds";
    public static PlaySoundProperty property;
    public static final Codec<List<Holder>> codec = AutoCodec.of(Holder.class).codec().listOf();

    public PlaySoundProperty() {
        super(KEY, codec);
        property = this;

        ApplicationEvents.ENTITY_RELATED.startListening(
                (event, entity, stack, data, originals) -> onEntityEvent(event, stack, entity, (Holder) data, originals),
                ApplicationEvents.StackGetterHolder.ofMulti(
                        property::get,
                        list -> list.stream().map(h -> Pair.of(h.item, h)).toList()
                )
        );
    }

    public void onEntityEvent(ApplicationEvent<?, ?, ?> event, ItemStack stack, Entity entity, Holder h, Object... originals) {
        if (!(entity.getWorld() instanceof ServerWorld world) || !h.event.equals(event)) return;

        Entity target = ApplicationEvents.getEntityForTarget(h.at, entity, event, originals);
        if (target == null) return;

        world.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                h.sound, getSoundCategory(h.category),
                h.volume, h.pitch
        );
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return array;
            }
        }
        return old;
    }

    public static class Holder {
        public SoundEvent sound;
        public @AutoCodec.Optional float pitch = 1;
        public @AutoCodec.Optional float volume = 1;
        public @AutoCodec.Optional String at = "this";
        public ApplicationEvent<?, ?, ?> event;
        public String item;
        public @AutoCodec.Optional String category = "master";

        public Holder() {}

        @Override
        public String toString() {
            return "Holder{" +
                    "sound=" + sound +
                    ", pitch=" + pitch +
                    ", volume=" + volume +
                    ", event=" + event +
                    ", item=" + item +
                    ", at=" + at +
                    '}';
        }
    }

    public static SoundCategory getSoundCategory(String str) {
        return switch (str) {
            case "music" -> SoundCategory.MUSIC;
            case "records" -> SoundCategory.RECORDS;
            case "weather" -> SoundCategory.WEATHER;
            case "blocks" -> SoundCategory.BLOCKS;
            case "hostile" -> SoundCategory.HOSTILE;
            case "neutral" -> SoundCategory.NEUTRAL;
            case "players" -> SoundCategory.PLAYERS;
            case "ambient" -> SoundCategory.AMBIENT;
            case "voice" -> SoundCategory.VOICE;
            default -> SoundCategory.MASTER;
        };
    }
}
