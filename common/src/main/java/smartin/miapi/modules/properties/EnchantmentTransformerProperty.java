package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentTransformerProperty implements ModuleProperty {
    public static String KEY = "enchantment_transformers";
    public static EnchantmentTransformerProperty property;


    public EnchantmentTransformerProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, EnchantmentTransformerProperty::getTransfomersCache);
        FakeEnchantment.enchantmentTransformers.add(this::transform);
        FakeEnchantment.adder.add(stack -> {
            List<Enchantment> enchantments = new ArrayList<>();
            getTransfomer(stack).forEach(transformer -> enchantments.add(transformer.enchantment));
            return enchantments;
        });
    }

    public int transform(Enchantment enchantment, ItemStack itemStack, int level) {
        for (EnchantMentTransformerData data : getTransfomer(itemStack)) {
            if(enchantment.equals(data.enchantment)){
                level = data.apply(level);
            }
        }
        return level;
    }

    private List<EnchantMentTransformerData> getTransfomer(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new ArrayList<>());
    }

    private static List<EnchantMentTransformerData> getTransfomersCache(ItemStack itemStack) {
        List<EnchantMentTransformerData> transformerData = new ArrayList<>();
        ItemModule.getModules(itemStack).allSubModules().forEach(moduleInstance -> {
            if (moduleInstance.getProperties().containsKey(property)) {
                moduleInstance.getProperties().get(property).getAsJsonArray().forEach(element -> {
                    transformerData.add(new EnchantMentTransformerData(element, moduleInstance));
                });
            }
        });
        return transformerData;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    public static class EnchantMentTransformerData {
        public Enchantment enchantment;
        public JsonObject json;
        public ModuleInstance moduleInstance;

        public EnchantMentTransformerData(JsonElement element, ModuleInstance moduleInstance) {
            json = element.getAsJsonObject();
            this.moduleInstance = moduleInstance;
            enchantment = Registries.ENCHANTMENT.get(new ResourceLocation(ModuleProperty.getString(json, "enchantment", moduleInstance, "")));
        }

        public int apply(int prevLevel) {
            if (json.get("level").isJsonPrimitive()) {
                String data = json.get("level").getAsString().replace("[old_level]", String.valueOf(prevLevel));
                prevLevel = (int) StatResolver.resolveDouble(data, moduleInstance);
            }
            return prevLevel;
        }

    }
}
