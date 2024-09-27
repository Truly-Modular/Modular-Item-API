package smartin.miapi.modules;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class StackStorageComponent {
    public static Codec<Map<String, ItemStack>> CODEC = Codec.unboundedMap(Codec.STRING, ItemStack.CODEC);
    public static DataComponentType<Map<String, ItemStack>> STACK_STORAGE_COMPONENT = DataComponentType .<Map<String, ItemStack>>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();
}
