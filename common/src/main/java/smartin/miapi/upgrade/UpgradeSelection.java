package smartin.miapi.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.List;
import java.util.Optional;

public record UpgradeSelection(List<String> slotLocation, ResourceLocation upgradeId) {
    public static final Codec<UpgradeSelection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("slot_location").forGetter(UpgradeSelection::slotLocation),
                    ResourceLocation.CODEC.fieldOf("upgrade_id").forGetter(UpgradeSelection::upgradeId)
            ).apply(instance, UpgradeSelection::new)
    );
    public static final StreamCodec<ByteBuf, UpgradeSelection> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    // Convenience constructor
    public UpgradeSelection(ModuleInstance moduleInstance, ResourceLocation upgradeId) {
        this(SlotProperty.getLocationSave(moduleInstance), upgradeId);
    }

    public Optional<ModuleInstance> resolveModule(ItemStack itemStack) {
        return SlotProperty.findSlot(itemStack, slotLocation).map(a -> a.inSlot);
    }
}
