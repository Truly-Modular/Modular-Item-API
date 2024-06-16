package smartin.miapi.modules;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static smartin.miapi.Miapi.LOGGER;
import static smartin.miapi.Miapi.gson;

@Deprecated
public class ModuleInstanceJsonAdapter extends TypeAdapter<ModuleInstance> {
    @Override
    public void write(JsonWriter out, ModuleInstance value) throws IOException {
        out.beginObject();
        out.name("module").value(value.module.name());
        if (value.moduleData != null) {
            out.name("moduleData").jsonValue(gson.toJson(value.moduleData));
        } else {
            Map<String, String> moduleData = new HashMap<>();
            out.name("moduleData").jsonValue(gson.toJson(moduleData));
        }
        if (value.subModules != null) {
            out.name("subModules").jsonValue(gson.toJson(value.subModules));
        } else {
            Map<String, String> subModules = new HashMap<>();
            out.name("subModules").jsonValue(gson.toJson(subModules));
        }
        out.endObject();
    }

    @Override
    public ModuleInstance read(JsonReader in) throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        String moduleKey = jsonObject.get("module").getAsString();
        ItemModule module = RegistryInventory.modules.get(moduleKey);
        if (module == null) {
            LOGGER.warn("Module not found for " + moduleKey + " Key - substituting with empty module");
            module = ItemModule.empty;
        }
        ModuleInstance moduleInstance = new ModuleInstance(module);
        moduleInstance.subModules = gson.fromJson(jsonObject.get("subModules"), new TypeToken<Map<Integer, ModuleInstance>>() {
        }.getType());
        if (moduleInstance.subModules != null) {
            moduleInstance.subModules.forEach((key, subModule) -> {
                subModule.parent = moduleInstance;
            });
        } else {
            moduleInstance.subModules = new HashMap<>();
        }
        moduleInstance.moduleData = gson.fromJson(jsonObject.get("moduleData"), new TypeToken<Map<String, String>>() {
        }.getType());
        if (moduleInstance.moduleData == null) {
            moduleInstance.moduleData = new HashMap<>();
        }
        return moduleInstance;
    }
}
