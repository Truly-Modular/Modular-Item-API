package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import smartin.miapi.Miapi;
import smartin.miapi.events.Event;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ApplicationEventHandler;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.event.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

public class PlaySoundProperty extends CodecBasedProperty<List<PlaySoundProperty.Holder>> implements ApplicationEventHandler {
    public static final String KEY = "playSounds";
    public static PlaySoundProperty property;

    public PlaySoundProperty() {
        super(KEY);

        ApplicationEvent.getAllEvents().forEach(e -> {
            e.addListener(this);
        });

        property = this;
    }

    @Override
    public <E> void onEvent(ApplicationEvent<E> main, E instance) {
        if (main instanceof ApplicationEvent.EntityHolding<E> event) {
            if (!(event.getEntity(instance).getWorld() instanceof ServerWorld world)) return;
            LivingEntity entity = event.getEntity(instance);

            ItemStack stack = event.stackGetter.apply(instance);
            ItemStack alternateStack = null;
            if (stack == null) {
                if (instance instanceof Event.LivingHurtEvent lh) {
                    stack = lh.getCausingItemStack();
                    alternateStack = lh.livingEntity.getMainHandStack();
                } else
                    stack = entity.getMainHandStack();
            }

            List<Holder> sounds = property.get(stack);
            List<Holder> alternate = alternateStack == null ? null : property.get(alternateStack);
            if (sounds == null) sounds = new ArrayList<>();
            if (alternate != null) {
                List<Holder> finalSounds = sounds;
                alternate.forEach(h -> finalSounds.add(new Holder(h.sound, h.pitch, h.volume, h.event, !h.target)));
            }

            sounds.forEach(h -> {
                if (event.equals(h.event) && h.target) {
                    world.playSound(
                            null,
                            entity.getX(), entity.getY(), entity.getZ(),
                            h.sound, SoundCategory.MASTER,
                            h.volume, h.pitch
                    );
                }
            });
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
                return Miapi.gson.toJsonTree(array);
            }
        }
        return old;
    }

    @Override
    public Codec<List<PlaySoundProperty.Holder>> codec(ItemModule.ModuleInstance instance) {
        return AutoCodec.of(Holder.class).codec().listOf();
    }

    public static class Holder {
        SoundEvent sound;
        @AutoCodec.Optional float pitch = 1;
        @AutoCodec.Optional float volume = 1;
        ApplicationEvent<?> event;
        @AutoCodec.Override("target_codec")
        @AutoCodec.Optional boolean target = true;

        public Holder() {}

        public Holder(SoundEvent sound, float pitch, float volume, ApplicationEvent<?> event, boolean targetIsMain) {
            this.sound = sound;
            this.pitch = pitch;
            this.volume = volume;
            this.event = event;
            this.target = targetIsMain;
        }

        public static final Codec<Boolean> target_codec = Codec.STRING.xmap(
                ApplicationEvent.EntityHolding::isTargetMain,
                bl -> bl ? "main" : "alternate"
        );


        @Override
        public String toString() {
            return "Holder{" +
                    "sound=" + sound +
                    ", pitch=" + pitch +
                    ", volume=" + volume +
                    ", event=" + event +
                    ", target=" + target +
                    '}';
        }
    }
}
