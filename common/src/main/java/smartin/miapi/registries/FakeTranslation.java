package smartin.miapi.registries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import smartin.miapi.datapack.ReloadEvents;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
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
