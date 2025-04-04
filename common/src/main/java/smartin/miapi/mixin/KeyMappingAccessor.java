package smartin.miapi.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor
    static Set<String> getCATEGORIES() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setCATEGORIES(Set<String> CATEGORIES) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static Map<String, Integer> getCATEGORY_SORT_ORDER() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setCATEGORY_SORT_ORDER(Map<String, Integer> CATEGORY_SORT_ORDER) {
        throw new UnsupportedOperationException();
    }
}
