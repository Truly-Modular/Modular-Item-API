package smartin.miapi.modules.properties.trinket;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.trinket.AdditionalSlotComponent;
import com.redpxnda.nucleus.trinket.NucleusTrinket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class TrinketSlotProperty extends CodecProperty<List<String>> implements ComponentApplyProperty {
    public static ResourceLocation KEY = Miapi.id("trinket_slots");
    public static final TrinketSlotProperty property = new TrinketSlotProperty();

    protected TrinketSlotProperty() {
        super(Codec.list(Codec.STRING));
        RegistryInventory.MODULAR_ITEMS.addCallback((i) -> {
            NucleusTrinket.register(i, new ModularTrinket());
        });
        NucleusTrinket.DONT_CRASH_ON_NO_CREATOR = true;
    }

    @Override
    public List<String> merge(List<String> left, List<String> right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {
        List<String> asd = getData(itemStack).orElse(List.of());
        if (asd.isEmpty()) {
            itemStack.remove(AdditionalSlotComponent.ADDITIONAL_SLOTS);
        } else {
            itemStack.set(AdditionalSlotComponent.ADDITIONAL_SLOTS,
                    new AdditionalSlotComponent(getData(itemStack).orElse(List.of())));
        }
    }
}
