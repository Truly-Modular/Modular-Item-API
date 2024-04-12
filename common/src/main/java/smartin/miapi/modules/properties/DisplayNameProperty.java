package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property allows modules to change the DisplayName of the item in question
 */
public class DisplayNameProperty implements ModuleProperty {
    public static final String KEY = "displayName";
    public static ModuleProperty property;

    public DisplayNameProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, DisplayNameProperty::resolveDisplayText);
    }

    public static Text getDisplayText(ItemStack stack) {
        return ModularItemCache.getVisualOnlyCache(stack, KEY, Text.empty());
    }

    private static Text resolveDisplayText(ItemStack itemStack) {
        String translationKey = "miapi.name.missing.nomodule";
        ItemModule.ModuleInstance root = ItemModule.getModules(itemStack);
        ItemModule.ModuleInstance primaryModule = ItemModule.getModules(itemStack);
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
