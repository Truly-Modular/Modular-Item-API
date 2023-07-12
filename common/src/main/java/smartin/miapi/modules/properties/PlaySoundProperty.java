package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CodecBasedEventProperty;
import smartin.miapi.modules.properties.util.PropertyApplication;

import java.util.List;

public class PlaySoundProperty extends CodecBasedEventProperty<List<PlaySoundProperty.Holder>> {
    public static final String KEY = "playSound";
    public static PlaySoundProperty property;

    public PlaySoundProperty() {
        super(
                KEY,
                new EventHandlingMap<>()
                        .setAll(PropertyApplication.ApplicationEvent.ABILITIES, PlaySoundProperty::onAbility)
        );

        property = this;
    }

    protected static void onAbility(PropertyApplication.ApplicationEvent<PropertyApplication.Ability> event, PropertyApplication.Ability ability) {
        if (!(ability.world() instanceof ServerWorld world)) return;

        List<Holder> sounds = property.get(ability.stack());
        if (sounds == null) return;

        sounds.forEach(h -> {
            if (event.equals(h.event)) {
                /*System.out.println(event.name);
                System.out.println(ability.world().isClient);*/
                world.playSound(
                        null,
                        ability.user().getX(), ability.user().getY(), ability.user().getZ(),
                        h.sound, SoundCategory.MASTER,
                        h.volume, h.pitch
                );
            }
        });
    }

    @Override
    public Codec<List<PlaySoundProperty.Holder>> codec(ItemModule.ModuleInstance instance) {
        return AutoCodec.of(Holder.class).codec().listOf();
    }

    public static class Holder {
        SoundEvent sound;
        @AutoCodec.Optional float pitch = 1;
        @AutoCodec.Optional float volume = 1;
        PropertyApplication.ApplicationEvent<?> event;
    }
}
