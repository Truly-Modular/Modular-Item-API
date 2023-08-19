package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * The Property controling {@link smartin.miapi.modules.abilities.RiptideAbility}
 */
public class RiptideProperty implements ModuleProperty {
    public static RiptideProperty property;
    public static final String KEY = "riptide";

    public RiptideProperty(){
        property = this;
    }


    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    public static RiptideJson getData(ItemStack itemStack){
        JsonElement element = ItemModule.getMergedProperty(itemStack,property);
        if(element == null){
            return null;
        }
        return Miapi.gson.fromJson(element,RiptideJson.class);
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type){
            case OVERWRITE -> {
                return toMerge;
            }
            case EXTEND,SMART -> {
                return old;
            }
        }
        return old;
    }

    public static class RiptideJson{
        public boolean needsWater = false;
        public boolean allowLava = false;
        public boolean needRiptideEnchant = true;
        public double riptideStrength = 3;
    }
}
