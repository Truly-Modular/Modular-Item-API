package smartin.miapi.modules.abilities.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.key.handler.BindingState;
import smartin.miapi.modules.abilities.key.handler.KeybindHandler;

public class MiapiBinding  {
    public static final Codec<MiapiBinding> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("id")
                                    .forGetter(binding -> binding.id),
                            Codec.STRING.fieldOf("category")
                                    .forGetter(binding -> binding.category),
                            Codec.INT.optionalFieldOf("scan_code", -1)
                                    .forGetter(binding -> binding.defaultScanCode),
                            KeybindHandler.HANDLER_CODEC.fieldOf("handler")
                                    .forGetter(binding -> binding.handler)
                    ).apply(instance, MiapiBinding::new)
            );

    public ResourceLocation id;
    public final String category;
    public final int defaultScanCode;
    public final KeybindHandler handler;
    public final BindingState state = new BindingState();
    @Environment(EnvType.CLIENT)
    private KeyMapping mapping;

    public  MiapiBinding(
            ResourceLocation id,
            String category,
            int defaultScanCode,
            KeybindHandler handler
    ) {
        this.id = id;
        this.category = category;
        this.defaultScanCode = defaultScanCode;
        this.handler = handler;
    }

    @Environment(EnvType.CLIENT)
    public KeyMapping asKeyMapping() {
        if (mapping == null) {
            mapping = new KeyMapping(Miapi.toLangString(id), defaultScanCode, category);
        }
        return mapping;
    }
}