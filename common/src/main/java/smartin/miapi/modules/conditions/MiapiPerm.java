package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MiapiPerm implements ModuleCondition {

    List<String> perms = new ArrayList<>();

    public MiapiPerm() {

    }

    public MiapiPerm(List<String> perms) {
        this.perms = perms;
    }

    @Override
    public boolean isAllowed(ItemModule.@Nullable ModuleInstance moduleInstance, @Nullable BlockPos tablePos, @Nullable PlayerEntity player, @Nullable Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        if(player != null && MiapiPermissions.hasPerm(player,perms)){
            return true;
        }
        reasons.add(Text.literal("This is a Cosmetic for Kofi and Patreon supporter."));
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        List<String> permList = new ArrayList<>();
        element.getAsJsonObject().get("perms").getAsJsonArray().forEach(element1 -> {
            permList.add(element1.getAsString());
        });
        return new MiapiPerm(permList);
    }
}
