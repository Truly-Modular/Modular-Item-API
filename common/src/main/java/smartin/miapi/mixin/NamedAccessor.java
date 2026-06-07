package smartin.miapi.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HolderSet.Named.class)
public interface NamedAccessor<T> {
    @Accessor("key")
    TagKey<T> getMiapiKey();

    @Invoker("bind")
    void callMiapiBind(List<Holder<T>> contents);
}
