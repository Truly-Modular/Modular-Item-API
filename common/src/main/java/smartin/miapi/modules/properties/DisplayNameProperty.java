package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property allows modules to change the DisplayName of the item in question
 */
public class DisplayNameProperty extends CodecBasedProperty<Component> {
    public static final String KEY = "displayName";
    public static ModuleProperty property;

    public DisplayNameProperty() {
        super(ComponentSerialization.CODEC);
        property = this;
        ModularItemCache.setSupplier(KEY, DisplayNameProperty::resolveDisplayText);
    }

    public static Component getDisplayText(ItemStack stack) {
        return ModularItemCache.getVisualOnlyCache(stack, KEY, Component.empty());
    }

    private static Component resolveDisplayText(ItemStack itemStack) {
        String translationKey = "miapi.name.missing.nomodule";
        ModuleInstance root = ItemModule.getModules(itemStack);
        ModuleInstance primaryModule = ItemModule.getModules(itemStack);
        for (ModuleInstance moduleInstance : root.allSubModules()) {
            JsonElement data = moduleInstance.getOldProperties().get(property);
            if (data != null) {
                translationKey = data.getAsString();
                primaryModule = moduleInstance;
            }
        }
        return StatResolver.translateAndResolve(translationKey, primaryModule);
    }

    @Override
    public Component merge(Component left, Component right, MergeType mergeType) {
        if(MergeType.EXTEND.equals(mergeType)){
            return left;
        }
        return right;
    }
}
