package smartin.miapi.modules.properties.util;

import java.util.List;

public interface Validator<T> {
    List<EditorError> validate(int line, T property, boolean isClient);
}