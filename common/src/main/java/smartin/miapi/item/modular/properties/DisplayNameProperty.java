package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.cache.ModularItemCache;

public class DisplayNameProperty implements ModuleProperty{
    public static final String key = "displayName";
    public static ModuleProperty displayNameProperty;

    public DisplayNameProperty(){
        displayNameProperty = this;
        ModularItemCache.setSupplier(key,(itemStack)->{
            return resolveDisplayText(itemStack);
        });
    }
    public static Text getDisplayText(ItemStack stack){
        return (Text) ModularItemCache.get(stack,key);
    }

    private static Text resolveDisplayText(ItemStack itemStack){
        ItemModule.ModuleInstance root = ModularItem.getModules(itemStack);
        root.allSubModules();
        String translationKey = "";
        ItemModule.ModuleInstance primaryModule = root;
        for(ItemModule.ModuleInstance moduleInstance: root.allSubModules()){
            JsonElement data = moduleInstance.getProperties().get(displayNameProperty);
            if(data!=null){
                translationKey = data.getAsString();
                primaryModule = moduleInstance;
            }
        }
        return StatResolver.translateAndResolve(translationKey,primaryModule);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
