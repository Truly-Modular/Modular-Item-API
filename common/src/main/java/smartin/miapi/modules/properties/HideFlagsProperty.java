package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class HideFlagsProperty implements ModuleProperty {
    public static String KEY = "hideFlags";
    public static HideFlagsProperty property;

    public HideFlagsProperty(){
        property = this;
    }

    public static Integer getHideProperty(Integer prevValue, ItemStack itemStack){
        JsonElement element = ItemModule.getMergedProperty(itemStack,property);
        if(element!=null && !element.isJsonNull() && element.isJsonPrimitive()){
            return element.getAsInt();
        }
        return prevValue;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsInt();
        return true;
    }
}
