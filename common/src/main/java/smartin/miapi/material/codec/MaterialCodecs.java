package smartin.miapi.material.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;

public final class MaterialCodecs {

    /**
     * Simple form: "modid:material"
     */
    private static final Codec<Material> SIMPLE = Codec.STRING.comapFlatMap(id -> {
        ResourceLocation rl;
        try {
            rl = Miapi.id(id);
        } catch (RuntimeException e) {
            return DataResult.error(() -> "Invalid material id: " + id);
        }

        Material material = MaterialProperty.MATERIAL_REGISTRY.get(rl);
        if (material == null) {
            return DataResult.error(() -> "Unknown material: " + rl);
        }

        return DataResult.success(material);
    }, material -> material.getID().toString());

    /**
     * Complex form: { "type": "modid:material", ... }
     * Dispatches to the material's MapCodec.
     */
    private static final Codec<Material> COMPLEX = Codec.STRING.dispatch("type", material -> material.getID().toString(), id -> {
        Material base = MaterialProperty.MATERIAL_REGISTRY.get(id);
        if (base == null) {
            throw new IllegalArgumentException("Unknown material type: " + id);
        }

        return base.codec().orElseThrow(() -> new IllegalStateException("Material " + id + " does not define a codec"));
    });

    private static final Codec<Material> DECODE = Codec.withAlternative(SIMPLE, COMPLEX);

    public static final Codec<Material> MATERIAL_CODEC = Codec.of(
            // Encoder
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(Material input, DynamicOps<T> ops, T prefix) {
                    // Route encoding explicitly
                    if (input.codec().isPresent() && input.codec().get() == null) {
                        Miapi.LOGGER.error("Material Codec is null but reported as present!");
                        if (input != null) {
                            Miapi.LOGGER.error(input.getClass().getName());
                        }
                        return SIMPLE.encode(input, ops, prefix);
                    } else if (input.codec().isPresent()) {
                        return COMPLEX.encode(input, ops, prefix);
                    } else {

                        return SIMPLE.encode(input, ops, prefix);
                    }
                }
            },
            // Decoder
            DECODE);

}
