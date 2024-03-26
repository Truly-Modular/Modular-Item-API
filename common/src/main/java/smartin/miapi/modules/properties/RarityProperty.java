package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class RarityProperty implements ModuleProperty {
    public static String KEY = "rarity";
    public static RarityProperty property;

    public RarityProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, RarityProperty::getRarityForCache);
    }

    public static Rarity getRarity(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularItem) {
            return ModularItemCache.get(itemStack, KEY, Rarity.COMMON);
        }
        return Rarity.COMMON;
    }

    private static Rarity getRarityForCache(ItemStack itemStack) {
        try {
            JsonElement jsonElement = ItemModule.getMergedProperty(itemStack, property);
            if (jsonElement != null) {
                return applyEnchant(itemStack, fromString(jsonElement.getAsString()));
            }
        } catch (Exception ignored) {

        }
        return applyEnchant(itemStack, Rarity.COMMON);
    }

    private static Rarity applyEnchant(ItemStack itemStack, Rarity old) {
        if (!itemStack.hasEnchantments()) {
            return old;
        } else {
            switch (old) {
                case COMMON:
                case UNCOMMON:
                    return Rarity.RARE;
                case RARE:
                    return Rarity.EPIC;
                case EPIC:
                default:
                    return old;
            }
        }
    }

    private static Rarity fromString(String string) {
        for (Rarity rarity : Rarity.values()) {
            if (rarity.toString().equalsIgnoreCase(string)) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }


    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
