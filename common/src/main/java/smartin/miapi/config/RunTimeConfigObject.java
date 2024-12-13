package smartin.miapi.config;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.config.ConfigObject;
import com.redpxnda.nucleus.config.ConfigType;
import com.redpxnda.nucleus.config.preset.ConfigPreset;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RunTimeConfigObject<T> extends ConfigObject<T> {
    /**
     * @param fileLocation   the path to the config
     * @param id             the name for the config
     * @param type           the type of config
     * @param codec          the codec used to (de)serialize the config
     * @param defaultCreator a supplier to create the default/empty version of this config
     * @param onUpdate       handler for when the config is updated
     * @param presetGetter   a getter for a preset
     * @param watch          if this config should be file-watched
     * @param instance       handler instance, null before first read
     */
    public RunTimeConfigObject(String fileLocation, ResourceLocation id, ConfigType type, Codec<T> codec, Supplier<T> defaultCreator, @Nullable Consumer<T> onUpdate, @Nullable Function<T, ConfigPreset<T, ?>> presetGetter, boolean watch, @Nullable T instance) {
        super(fileLocation, id, type, codec, defaultCreator, onUpdate, presetGetter, watch, instance);
    }

    @Override
    public void save() {
        //this is not meant to save to file
    }
}
