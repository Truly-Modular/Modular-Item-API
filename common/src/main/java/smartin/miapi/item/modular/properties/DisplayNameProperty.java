package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.cache.ModularItemCache;

public class DisplayNameProperty implements ModuleProperty {
    public static final String KEY = "displayName";
    public static ModuleProperty property;

    public DisplayNameProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, DisplayNameProperty::resolveDisplayText);
    }

    public static Text getDisplayText(ItemStack stack) {
        return (Text) ModularItemCache.get(stack, KEY);
    }

    private static Text resolveDisplayText(ItemStack itemStack) {
        ItemModule.ModuleInstance root = ItemModule.getModules(itemStack);
        root.allSubModules();
        String translationKey = "";
        ItemModule.ModuleInstance primaryModule = root;
        for (ItemModule.ModuleInstance moduleInstance : root.allSubModules()) {
            JsonElement data = moduleInstance.getProperties().get(property);
            if (data != null) {
                translationKey = data.getAsString();
                primaryModule = moduleInstance;
            }
        }
        return StatResolver.translateAndResolve(translationKey, primaryModule);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case EXTEND -> {
                return old;
            }
            case SMART, OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }
}
