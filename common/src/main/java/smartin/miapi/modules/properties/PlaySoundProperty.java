package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaySoundProperty extends CodecBasedProperty<List<PlaySoundProperty.Holder>> {
    public static final String KEY = "playSounds";
    public static PlaySoundProperty property;
    public static final Codec<List<Holder>> codec = AutoCodec.of(Holder.class).codec().listOf();

    public PlaySoundProperty() {
        super(KEY);
        property = this;

        ApplicationEvents.ENTITY_RELATED.startListening(
                (event, entity, stack, data, originals) -> onEntityEvent(event, stack, entity, (List<Holder>) data, originals),
                ApplicationEvents.StackGetterHolder.ofMulti(
                        property::get,
                        list -> list.stream().map(h -> h.item).toList(),
                        (list, target) -> list.stream().filter(d -> d.item.equals(target)).toList()
                )
        );
    }

    public void onEntityEvent(ApplicationEvent<?, ?, ?> event, ItemStack stack, Entity entity, List<Holder> sounds, Object... originals) {
        if (!(entity.getWorld() instanceof ServerWorld world)) return;

        Map<String, Entity> validEntities = new HashMap<>();
        validEntities.put("this", entity);
        if (event instanceof ApplicationEvents.HurtEvent) {
            DamageSource damageSource = (DamageSource) originals[1];
            LivingEntity victim = (LivingEntity) originals[0];

            validEntities.put("victim", victim);
            if (damageSource != null) {
                if (damageSource.getAttacker() != null) validEntities.put("attacker", damageSource.getAttacker());
                if (damageSource.getSource() != null) validEntities.put("source", damageSource.getSource());
            }
        }

        for (Holder h : sounds) {
            if (!h.event.equals(event)) continue;

            Entity target = ApplicationEvents.getEntityForTarget(h.at, validEntities, entity);
            if (target == null) continue;

            world.playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    h.sound, SoundCategory.MASTER,
                    h.volume, h.pitch
            );
        }
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

    @Override
    public Codec<List<PlaySoundProperty.Holder>> codec(ItemModule.ModuleInstance instance) {
        return codec;
    }

    public static class Holder {
        public SoundEvent sound;
        public @AutoCodec.Optional float pitch = 1;
        public @AutoCodec.Optional float volume = 1;
        public @AutoCodec.Optional String at = "this";
        public ApplicationEvent<?, ?, ?> event;
        public String item;

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
}
