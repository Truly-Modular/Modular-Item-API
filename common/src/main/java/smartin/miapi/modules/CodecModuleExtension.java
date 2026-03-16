package smartin.miapi.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.datapack.HierarchicalReloadBuilder;

import java.util.HashMap;

/**
 * A datapack-defined inheritance/extension for ItemModules.
 */
public record CodecModuleExtension(ResourceLocation target,
                                   PropertyHolder extensionData) implements HierarchicalReloadBuilder.Extension<ItemModule> {

    public static final Codec<CodecModuleExtension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("parent").forGetter(CodecModuleExtension::target),
            PropertyHolder.MAP_CODEC.fieldOf("data").forGetter(CodecModuleExtension::extensionData)
    ).apply(instance, CodecModuleExtension::new));

    @Override
    public ItemModule applyTo(ItemModule base) {
        // Merge the new properties into the base module’s PropertyHolder.
        var mergedHolder = extensionData.applyHolder(new HashMap<>(base.properties()), java.util.Optional.empty());
        return new ItemModule(base.id(), mergedHolder);
    }
}
