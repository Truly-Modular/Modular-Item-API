package smartin.miapi.injections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redpxnda.nucleus.datapack.codec.InterfaceDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyInjector {
    public static final Map<String, TargetSelector> targetSelectors = new HashMap<>();
    public static final InterfaceDispatcher<TargetSelector> targetSelectionDispatcher = InterfaceDispatcher.of(targetSelectors, "type");

    public interface TargetSelector {
        List<JsonObject> inject(JsonElement element);
    }

    static {
        /*targetSelectors.put("module", element -> {
            if (element.)
        });*/
    }
}
