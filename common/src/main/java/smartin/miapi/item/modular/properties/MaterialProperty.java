package smartin.miapi.item.modular.properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialProperty extends CraftingProperty {

    public static String key = "material";

    public static ModuleProperty materialProperty;

    public static Map<String, JsonElement> materialMap = new HashMap<>();

    public MaterialProperty() {
        materialProperty = this;
        StatResolver.registerResolver(key, new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getKeyedProperties().get(key);
                try {
                    if (jsonData != null) {
                        jsonData = materialMap.get(jsonData.getAsString());
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsDouble();
                            }
                        }
                    }
                } catch (Exception suppressed) {

                }
                return 0;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getProperties().get(materialProperty);
                try {
                    if (jsonData != null) {
                        jsonData = materialMap.get(jsonData.getAsString());
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsString();
                            }
                        }
                    }
                } catch (Exception suppressed) {

                }
                return "";
            }
        });
        ReloadEvents.DataPackLoader.subscribe((path, data) -> {
            if (path.contains("material")) {
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(data).getAsJsonObject();
                materialMap.put(obj.get("key").getAsString(), obj);
            }
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Nullable
    public String getMaterial(ItemModule.ModuleInstance instance){
        JsonElement element = instance.getProperties().get(this);
        if(element!=null){
            return element.getAsString();
        }
        return null;
    }

    public void setMaterial(ItemModule.ModuleInstance instance,String material){
        String propertyString = instance.moduleData.computeIfAbsent("properties",(key)->{
            return "{material:empty}";
        });
        JsonObject moduleJson = Miapi.gson.fromJson( propertyString, JsonObject.class);
        moduleJson.addProperty("material",material);
        instance.moduleData.put("properties",moduleJson.getAsString());
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory) {
        return null;
    }
}
