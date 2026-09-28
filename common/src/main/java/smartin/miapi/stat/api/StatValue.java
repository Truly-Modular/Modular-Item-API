package smartin.miapi.stat.api;

import net.minecraft.world.item.ItemStack;

public record StatValue<T, M extends StatMetaData<T>>(
        Stat<T, M> stat,
        T value
) {
    public static record StatWithValues<T, M extends StatMetaData<T>>(Stat<T, M> stat, StatValue<T, M> baseItemValue,
                                                                      StatValue<T, M> compareItemValue) {
        public StatWithValues(Stat<T, M> stat, ItemStack base, ItemStack compare) {
            this(
                    stat,
                    new StatValue<>(stat, stat.metaData().getData(base)),
                    new StatValue<>(stat, stat.metaData().getData(compare)));
        }
    }
}