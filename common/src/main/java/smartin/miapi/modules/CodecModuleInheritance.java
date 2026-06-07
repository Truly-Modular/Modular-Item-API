package smartin.miapi.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.datapack.HierarchicalReloadBuilder;

import java.util.HashMap;

/**
 * A datapack-defined inheritance/extension for ItemModules.
 */
public record CodecModuleInheritance(ResourceLocation target,
                                     PropertyHolder extensionData) implements HierarchicalReloadBuilder.Extension<ItemModule> {

    public static final Codec<CodecModuleInheritance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("parent").forGetter(CodecModuleInheritance::target),
            PropertyHolder.MAP_CODEC.fieldOf("data").forGetter(CodecModuleInheritance::extensionData)
    ).apply(instance, CodecModuleInheritance::new));

    @Override
    public ItemModule applyTo(ItemModule base) {
        // Merge the new properties into the base module’s PropertyHolder.
        var mergedHolder = extensionData.applyHolder(new HashMap<>(base.properties()), java.util.Optional.empty());
        return new ItemModule(base.id(), mergedHolder);
    }
}
