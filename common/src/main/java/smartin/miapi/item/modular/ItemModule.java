package smartin.miapi.item.modular;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvent;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemModule {
    public static final MiapiRegistry<ItemModule> moduleRegistry = MiapiRegistry.getInstance(ItemModule.class);
    private final String name;
    private final Map<String, JsonElement> properties;

    public ItemModule(String name, Map<String, JsonElement> properties) {
        this.name = name;
        this.properties = properties;
    }

    public static void loadFromData(String path, String moduleJsonString) {
        Gson gson = new Gson();
        JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);

        String name = moduleJson.get("name").getAsString();
        Map<String, JsonElement> moduleProperties = new HashMap<>();

        processModuleJsonElement(moduleJson, moduleProperties, name,path,moduleJsonString);

        moduleRegistry.register(name,new ItemModule(name, moduleProperties));
    }

    protected static void processModuleJsonElement(JsonElement element, Map<String, JsonElement> moduleProperties, String name, String path, String rawString) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                ModuleProperty property = Miapi.modulePropertyRegistry.get(key);
                if (property!=null) {
                    if(isValidProperty(key,name,value)){
                        moduleProperties.put(key, value);
                    }
                } else if (value.isJsonObject()) {
                    processModuleJsonElement(value, moduleProperties, name, path, rawString);
                } else if (value.isJsonArray()) {
                    JsonArray jsonArray = value.getAsJsonArray();
                    for (JsonElement jsonElement : jsonArray) {
                        processModuleJsonElement(jsonElement, moduleProperties, name, path, rawString);
                    }
                }
                else{
                    Miapi.LOGGER.error("Error while reading ModuleJson, module "+name+" key/property "+key+" in file "+path+ " Please make sure there are no Typos in the Property Names");
                }
            }
        }
    }

    protected static boolean isValidProperty(String key,String moduleKey,JsonElement data) {
        ModuleProperty property = Miapi.modulePropertyRegistry.get(key);
        if(property!=null){
            try{
                return property.load(moduleKey,data);
            }catch (Exception e){
                e.printStackTrace();
                RuntimeException exception = new RuntimeException("Failure during moduleLoad, Error in Module "+moduleKey+" with property "+key+" with data "+data+ " with error "+e.getLocalizedMessage());
                throw exception;
            }
        }
        return false;
    }

    public Map<String, JsonElement> getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    @JsonAdapter(ModuleInstanceJsonAdapter.class)
    public static class ModuleInstance{
        @Nullable
        public ModuleInstance parent;
        public ItemModule module;
        public Map<Integer,ModuleInstance> subModules;
        public Map<String,String> moduleData = new HashMap<>();

        public List<ModuleInstance> allSubModules() {
            List<ModuleInstance> moduleInstances = new ArrayList<>();
            moduleInstances.add(this);
            if(subModules!=null){
                this.subModules.forEach((id,subModule)->{
                    moduleInstances.addAll(subModule.allSubModules());
                });
            }
            return moduleInstances;
        }

        public ModuleInstance(ItemModule module){
            this.module = module;
        }

        public String toString(){
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public static ModuleInstance fromString(String string){
            Gson gson = new Gson();
            return gson.fromJson(string,ModuleInstance.class);
        }
    }

    public static class ModuleInstanceJsonAdapter extends TypeAdapter<ModuleInstance> {
        @Override
        public void write(JsonWriter out, ModuleInstance value) throws IOException {
            out.beginObject();
            out.name("module").value(value.module.name);
            if(value.moduleData!=null){
                out.name("moduleData").jsonValue(new Gson().toJson(value.moduleData));
            }
            else{
                Map<String,String> moduleData = new HashMap<>();
                out.name("moduleData").jsonValue(new Gson().toJson(moduleData));
            }
            if(value.subModules!=null){
                out.name("subModules").jsonValue(new Gson().toJson(value.subModules));
            }
            else{
                Map<String,String> subModules = new HashMap<>();
                out.name("subModules").jsonValue(new Gson().toJson(subModules));
            }
            out.endObject();
        }

        @Override
        public ModuleInstance read(JsonReader in) throws IOException {
            JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
            String moduleKey = jsonObject.get("module").getAsString();
            ItemModule module = moduleRegistry.get(moduleKey);
            ModuleInstance moduleInstance = new ModuleInstance(module);
            moduleInstance.subModules = new Gson().fromJson(jsonObject.get("subModules"), new TypeToken<Map<Integer,ModuleInstance>>(){}.getType());
            if(moduleInstance.subModules!=null){
                moduleInstance.subModules.forEach((key,subModule)->{
                    subModule.parent = moduleInstance;
                });
            }
            moduleInstance.moduleData = new Gson().fromJson(jsonObject.get("moduleData"), new TypeToken<Map<String, String>>(){}.getType());
            return moduleInstance;
        }
    }
}
