package smartin.miapi.client.gui.crafting.statdisplay.rework.api;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.registries.MiapiRegistry;

public class StatType<T, M extends StatMetaData> {
    public static final MiapiRegistry<StatType> STAT_TYPE_REGISTRY = MiapiRegistry.getInstance(StatType.class);

    public static <T, M extends StatMetaData<T>> StatType<T, M> register(
            ResourceLocation id,
            Class<T> valueClass,
            Class<M> metaDataClass
    ) {
        return (StatType<T, M>) STAT_TYPE_REGISTRY.register(id, new StatType<>(id, valueClass, metaDataClass));
    }

    ResourceLocation id;
    Class<T> valueClass;
    Class<M> metaDataClass;

    protected StatType(
            ResourceLocation id,
            Class<T> valueClass,
            Class<M> metaDataClass) {
        this.id = id;
        this.valueClass = valueClass;
        this.metaDataClass = metaDataClass;
    }
}