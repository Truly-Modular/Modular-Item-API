package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class BlockPoseProperty implements ModuleProperty {
    public static BlockPoseProperty poseProperty;
    public static final String KEY = "block_pose";

    public BlockPoseProperty() {
        poseProperty = this;
    }

    public String getPoseId(ItemStack itemStack) {
        var data = getJsonElement(itemStack);
        if (data != null) {
            try {
                return data.getAsString();
            } catch (RuntimeException e) {

            }
        }
        return "miapi:block";
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
