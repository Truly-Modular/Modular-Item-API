package smartin.miapi.mixin;

import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HolderSet.Named.class)
public interface NamedAccessor<T> {
    @Accessor
    TagKey<T> getKey();
}
