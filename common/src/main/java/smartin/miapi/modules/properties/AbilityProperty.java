package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.event.PropertyApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * This property manages the active {@link PropertyApplication.Ability}
 */
public class AbilityProperty extends CodecBasedProperty<Map<ItemUseAbility, Identifier>> {
    public static final Codec<Map<ItemUseAbility, Identifier>> CODEC = Codec.either(
            Codec.unboundedMap(ItemUseAbility.codec, Identifier.CODEC),
            ItemUseAbility.codec.listOf().xmap(l -> {
                Map<ItemUseAbility, Identifier> map = new HashMap<>();
                l.forEach(ab -> map.put(ab, null));
                return map;
            }, m -> m.keySet().stream().toList())
    ).xmap(e -> {
        if (e.right().isPresent()) return e.right().get();
        else return e.left().get();
    }, Either::left);

    public static final String KEY = "abilities";
    public static AbilityProperty property;

    public AbilityProperty() {
        super(KEY);

        property = this;
    }

    public static Map<ItemUseAbility, Identifier> getStatic(ItemStack stack) {
        return property.get(stack);
    }

    public Map<ItemUseAbility, Identifier> get(ItemStack itemStack) {
        Map<ItemUseAbility, Identifier> map = super.get(itemStack);
        if (map != null) return map;

        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        return CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to parse(during get) the AbilityProperty for a module! -> {}", s));
    }

    @Override
    public Codec<Map<ItemUseAbility, Identifier>> codec(ItemModule.ModuleInstance instance) {
        return CODEC;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) {
        CODEC.parse(JsonOps.INSTANCE, data).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to parse(during load) the AbilityProperty for a module! -> {}", s));
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                if (old.isJsonArray()) {
                    old = listToObject(old);
                }
                JsonObject object = old.deepCopy().getAsJsonObject();
                listToObject(toMerge).getAsJsonObject().asMap().forEach(object::add);

                return object;
            }
        }
        return old;
    }

    protected static JsonObject listToObject(JsonElement element) {
        if (element.isJsonArray()) {
            JsonObject object = new JsonObject();
            element.deepCopy().getAsJsonArray().forEach(e -> object.addProperty(e.getAsString(), ".none"));
            return object;
        }
        return element.getAsJsonObject().deepCopy();
    }
}
