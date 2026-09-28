package smartin.miapi.client.gui.crafting.statdisplay.rework.api;

public record StatValue<T, M extends StatMetaData<T>>(
        Stat<T, M> stat,
        T value
) {
}