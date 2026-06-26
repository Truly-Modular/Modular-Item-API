package smartin.miapi.registries.codec;

public record MiapiHolder<T>(MiapiType<T> type, T value) {
}