package smartin.miapi.blueprint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonAdapter(Blueprint.BlueprintJsonAdapter.class)
public class Blueprint {
    public static MiapiRegistry<BlueprintCondition> blueprintConditionRegistry = MiapiRegistry.getInstance(BlueprintCondition.class);
    public ItemModule module = ItemModule.empty;
    public String key;
    public int level = 0;
    public Map<ModuleProperty, JsonElement> upgrades = new HashMap<>();
    public BlueprintCondition isAllowed = AlwaysAllowedCondition.ALWAYS_ALLOWED;

    public void writeToElement() {

    }

    public static class BlueprintJsonAdapter extends TypeAdapter<Blueprint> {

        @Override
        public void write(JsonWriter jsonWriter, Blueprint blueprint) throws IOException {

        }

        @Override
        public Blueprint read(JsonReader jsonReader) throws IOException {
            JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
            Blueprint blueprint = new Blueprint();
            String key = jsonObject.get("key").getAsString();
            blueprint.key = key;
            if (jsonObject.has("module")) {
                blueprint.module = RegistryInventory.modules.get(jsonObject.get("module").getAsString());
            } else {
                Miapi.LOGGER.warn("Blueprints target module is invalid. Either Module is not loaded or Blueprint is setup wrong. " + key);
            }
            if (jsonObject.has("condition")) {
                JsonObject conditionElement = jsonObject.get("condition").getAsJsonObject();
                String asd = conditionElement.get("key").getAsString();
                blueprint.isAllowed = blueprintConditionRegistry.get(asd).fromJson(conditionElement);
            } else {
                Miapi.LOGGER.warn("Blueprints target condition is invalid. Either Condition is not loaded or Blueprint is setup wrong. " + key);
            }
            return blueprint;
        }
    }

    public static abstract class BlueprintCondition {

        public abstract BlueprintCondition fromJson(JsonElement element);

        public abstract boolean isAllowed();
    }

    public static class AlwaysAllowedCondition extends BlueprintCondition {
        public static AlwaysAllowedCondition ALWAYS_ALLOWED = new AlwaysAllowedCondition();

        private AlwaysAllowedCondition() {
        }

        @Override
        public BlueprintCondition fromJson(JsonElement element) {
            return ALWAYS_ALLOWED;
        }

        @Override
        public boolean isAllowed() {
            return true;
        }
    }
}
