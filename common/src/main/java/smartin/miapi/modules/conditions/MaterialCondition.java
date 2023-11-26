package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public class MaterialCondition implements ModuleCondition {
    public String material = "";

    public MaterialCondition() {

    }

    public MaterialCondition(String material) {
        this.material = material;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        JsonElement data = propertyMap.get(MaterialProperty.property);
        if (data == null) {
            reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.material.error"));
            return false;
        }
        Material material1 = MaterialProperty.getMaterialFromIngredient(data);
        if (material1 != null && MaterialProperty.getMaterialFromIngredient(data).getKey().equals(material)) {
            return true;
        }
        reasons.add(Text.translatable(Miapi.MOD_ID + ".condition.material.error"));
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new MaterialCondition(element.getAsJsonObject().get("material").getAsString());
    }
}
