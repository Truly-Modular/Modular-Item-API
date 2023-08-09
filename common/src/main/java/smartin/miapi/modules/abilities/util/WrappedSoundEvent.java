package smartin.miapi.modules.abilities.util;

import net.minecraft.sound.SoundEvent;

public record WrappedSoundEvent(SoundEvent event, float volume, float pitch) {
}
