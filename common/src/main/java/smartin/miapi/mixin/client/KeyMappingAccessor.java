package smartin.miapi.mixin.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("CATEGORIES")
    static Set<String> getMiapiCategories() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor("CATEGORIES")
    static void setMiapiCategories(Set<String> CATEGORIES) {
        throw new UnsupportedOperationException();
    }

    @Accessor("CATEGORY_SORT_ORDER")
    static Map<String, Integer> getMiapiCategoryOrder() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor("CATEGORY_SORT_ORDER")
    static void setMiapiCategoryOrder(Map<String, Integer> CATEGORY_SORT_ORDER) {
        throw new UnsupportedOperationException();
    }
}
