package smartin.miapi.registries;

import smartin.miapi.datapack.ReloadEvents;

import java.util.HashMap;
import java.util.Map;

public class FakeTranslation {
    public static Map<String, String> translations = new HashMap<>();

    static {
        ReloadEvents.START.subscribe(isClient -> {
            if (isClient) {
                translations.clear();
            }
        });
    }
}
