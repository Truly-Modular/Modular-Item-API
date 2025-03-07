package smartin.miapi.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor
    static Codec<ItemStack> getCODEC() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setCODEC(Codec<ItemStack> CODEC) {
        throw new UnsupportedOperationException();
    }
}
