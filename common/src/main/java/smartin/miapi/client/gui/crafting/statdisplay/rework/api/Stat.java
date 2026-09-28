package smartin.miapi.client.gui.crafting.statdisplay.rework.api;

import net.minecraft.resources.ResourceLocation;
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
}