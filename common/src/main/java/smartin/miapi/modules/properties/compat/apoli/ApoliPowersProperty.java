package smartin.miapi.modules.properties.compat.apoli;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.platform.Platform;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * This property manages the active {@link ItemUseAbility}
 */
public class ApoliPowersProperty implements ModuleProperty {
    public static final String KEY = "apoli_powers";
    public static ApoliPowersProperty property;

    public ApoliPowersProperty() {
        property = this;
    }

    public static List<PowerJson> getPowerJson(ItemStack itemStack) {
        List<PowerJson> powers = new ArrayList<>();
        if (itemStack.getItem() instanceof ModularItem) {
            JsonElement json = ItemModule.getMergedProperty(itemStack, property);
            if (json != null) {
                json.getAsJsonArray().forEach(jsonElement -> {
                    powers.add(new PowerJson(jsonElement.getAsJsonObject()));
                });
            }
        }
        return powers;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonArray().forEach(jsonElement -> new PowerJson(jsonElement.getAsJsonObject()));
        return Platform.isModLoaded("apoli");
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return Miapi.gson.toJsonTree(array);
            }
        }
        return old;
    }

    public static class PowerJson {
        public EquipmentSlot slot;
        public ResourceLocation powerId;
        public boolean isHidden;
        public boolean isNegative;

        public PowerJson(JsonObject element) {
            slot = AttributeProperty.getSlot(element.get("slot").getAsString());
            powerId = new ResourceLocation(element.get("powerId").getAsString());
            isHidden = element.get("isHidden").getAsBoolean();
            isNegative = element.get("isNegative").getAsBoolean();
        }
    }
}
