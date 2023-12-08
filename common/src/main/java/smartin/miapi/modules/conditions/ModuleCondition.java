package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import smartin.miapi.Miapi;

import java.io.IOException;
@JsonAdapter(ModuleCondition.ModuleConditionJsonAdapter.class)
public interface ModuleCondition {

    boolean isAllowed(ConditionManager.ConditionContext conditionContext);

    ModuleCondition load(JsonElement element);

    class ModuleConditionJsonAdapter extends TypeAdapter<ModuleCondition> {

        @Override
        public void write(JsonWriter jsonWriter, ModuleCondition moduleCondition) throws IOException {
            //This should never need to write
        }

        @Override
        public ModuleCondition read(JsonReader jsonReader) throws IOException {
            return ConditionManager.get(Miapi.gson.fromJson(jsonReader,JsonElement.class));
        }
    }
}
