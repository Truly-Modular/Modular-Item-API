package smartin.miapi.stat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.registries.DatapackMiapiRegistry;

public record Stat<T, M extends StatMetaData<T>>(
        ResourceLocation id,
        StatText<T, M> name,
        StatText<T, M> description,
        StatType<T, M> type,
        StatGroup group,
        M metaData
) {
    public static final DatapackMiapiRegistry<Stat<?, ?>> STAT_REGISTRY = new DatapackMiapiRegistry<>(Stat.class);

    public Component getDescription(ItemStack stack) {
        return description().get(stack, metaData().getData(stack), metaData());
    }

    public Component getNameWithPrefix(ItemStack stack) {
        MutableComponent component = Component.empty();
        component.append(group().getPrefix());
        component.append(name().get(stack, metaData().getData(stack), metaData()));
        return component;
    }

    public Component getName(ItemStack stack) {
        return name().get(stack, metaData().getData(stack), metaData());
    }
}