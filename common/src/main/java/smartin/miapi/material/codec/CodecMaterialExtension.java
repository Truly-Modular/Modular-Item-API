package smartin.miapi.material.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.HierarchicalReloadBuilder;
import smartin.miapi.registries.JsonOpsBooleanPatched;

/**
 * A datapack-defined extension for an existing material.
 * It references a "parent" material and merges additional fields into it,
 * producing a new CodecMaterial.
 */
public class CodecMaterialExtension implements HierarchicalReloadBuilder.Extension<CodecMaterial> {
    private final ResourceLocation parent;
    private final CodecMaterial extension;

    public CodecMaterialExtension(ResourceLocation parent, CodecMaterial extension) {
        this.parent = parent;
        this.extension = extension;
    }

    @Override
    public ResourceLocation target() {
        return parent;
    }

    @Override
    public CodecMaterial applyTo(CodecMaterial base) {
        CodecMaterial copy = base.copy();
        copy.merge(extension);
        return copy;
    }

    public static final Codec<CodecMaterialExtension> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("parent").forGetter(CodecMaterialExtension::target),
                    CodecMaterial.CODEC.fieldOf("data").forGetter(ext -> ext.extension)
            ).apply(instance, CodecMaterialExtension::new));

    public static CodecMaterialExtension decode(String data, RegistryAccess registryAccess) {
        var ops = RegistryOps.create(JsonOpsBooleanPatched.INSTANCE, registryAccess);
        return CODEC.decode(ops, Miapi.gson.fromJson(data, com.google.gson.JsonObject.class))
                .getOrThrow(err -> new DecoderException("Could not decode Material Extension: " + err))
                .getFirst();
    }
}
