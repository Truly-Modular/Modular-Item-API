package smartin.miapi.modules.properties.util;

import net.minecraft.network.chat.Component;

public interface SourceSetter<T> {


    T setSource(T data, Component source);

    default Object setSourceCast(Object object, Component source) {
        return setSource(cast(object), source);
    }

    @SuppressWarnings("unchecked")
    default T cast(Object object) {
        return (T) object;
    }
}
