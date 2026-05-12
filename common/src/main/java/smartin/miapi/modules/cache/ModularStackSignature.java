package smartin.miapi.modules.cache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ModularStackSignature {
    private static final long FNV_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    private ModularStackSignature() {
    }

    public static long capture(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0L;
        }

        long hash = FNV_OFFSET_BASIS;
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        hash = mix(hash, itemId == null ? "minecraft:air" : itemId.toString());
        hash = mix(hash, stack.getCount());
        hash = mix(hash, stack.getDamage());

        NbtCompound tag = stack.getNbt();
        hash = mix(hash, tag == null ? 0 : tag.hashCode());
        return hash;
    }

    private static long mix(long hash, int value) {
        hash ^= value;
        hash *= FNV_PRIME;
        return hash;
    }

    private static long mix(long hash, String value) {
        long mixed = hash;
        for (int i = 0; i < value.length(); i++) {
            mixed ^= value.charAt(i);
            mixed *= FNV_PRIME;
        }
        return mixed;
    }
}
