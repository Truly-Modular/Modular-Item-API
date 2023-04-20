package smartin.miapi.item.modular;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.attribute.EntityAttribute;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.properties.AttributeProperty;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemModule {
    private static final MiapiRegistry<ItemModule> moduleRegistry = MiapiRegistry.getInstance(ItemModule.class);
    private final String name;
    private final Map<String, JsonElement> properties;
    public static final ItemModule empty = new ItemModule("empty", new HashMap<>());

    public ItemModule(String name, Map<String, JsonElement> properties) {
        this.name = name;
        this.properties = properties;
    }

    public static void loadFromData(String path, String moduleJsonString) {
        Gson gson = new Gson();
        JsonObject moduleJson = gson.fromJson(moduleJsonString, JsonObject.class);
        if(!path.contains("modules")){
            return;
        }

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

    public static List<ModuleInstance> createFlatList(ModuleInstance root) {
        List<ModuleInstance> flatList = new ArrayList<>();
        List<ModuleInstance> queue = new ArrayList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            ModuleInstance module = queue.remove(0);
            if(module!=null){
                flatList.add(module);

                List<ModuleInstance> submodules = new ArrayList<>();
                module.subModules.keySet().stream().sorted((a,b)->b-a).forEach(id-> {
                    submodules.add(module.subModules.get(id));
                });
                queue.addAll(0, submodules);
            }
        }

        return flatList;
    }

    @JsonAdapter(ModuleInstanceJsonAdapter.class)
    public static class ModuleInstance{
        public ItemModule module;
        @Nullable
        public ModuleInstance parent;
        public Map<Integer,ModuleInstance> subModules = new HashMap<>();
        public Map<String,String> moduleData = new HashMap<>();

        public List<ModuleInstance> allSubModules() {
            return ItemModule.createFlatList(this);
        }

        public Map<ModuleProperty, JsonElement> getProperties() {
            return PropertyResolver.resolve(this);
        }

        public Map<String,JsonElement> getKeyedProperties(){
            Map<String,JsonElement> map = new HashMap<>();
            getProperties().forEach((property, jsonElement) -> {
                map.put(Miapi.modulePropertyRegistry.findKey(property),jsonElement);
            });
            return map;
        }

        public ModuleInstance getRoot(){
            ModuleInstance root = this;
            while (root.parent!=null){
                root = root.parent;
            }
            return root;
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
            ModuleInstance moduleInstance = gson.fromJson(string,ModuleInstance.class);
            if(moduleInstance.module==null){
                moduleInstance.module = ItemModule.empty;
            }
            return moduleInstance;
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
            if(module==null) {
                module = ItemModule.empty;
            }
            ModuleInstance moduleInstance = new ModuleInstance(module);
            moduleInstance.subModules = new Gson().fromJson(jsonObject.get("subModules"), new TypeToken<Map<Integer,ModuleInstance>>(){}.getType());
            if(moduleInstance.subModules!=null){
                moduleInstance.subModules.forEach((key,subModule)->{
                    subModule.parent = moduleInstance;
                });
            }
            else{
                moduleInstance.subModules = new HashMap<>();
            }
            moduleInstance.moduleData = new Gson().fromJson(jsonObject.get("moduleData"), new TypeToken<Map<String, String>>(){}.getType());
            if(moduleInstance.moduleData==null){
                moduleInstance.moduleData = new HashMap<>();
            }
            return moduleInstance;
        }
    }
}
