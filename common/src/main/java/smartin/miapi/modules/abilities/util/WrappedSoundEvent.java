package smartin.miapi.modules.abilities.util;

import net.minecraft.sounds.SoundEvent;

public record WrappedSoundEvent(SoundEvent event, float volume, float pitch) {
}
