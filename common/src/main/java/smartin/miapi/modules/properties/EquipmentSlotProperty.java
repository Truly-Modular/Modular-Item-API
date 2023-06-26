package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;


/**
 * This property allows to dynamically set teh preferred EquipmentSlot
 */
public class EquipmentSlotProperty implements ModuleProperty {
    public static final String KEY = "equipmentSlot";
    public static ModuleProperty property;

    public EquipmentSlotProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        String name = data.getAsString();
        return true;
    }

    /**
     * Should this use the Cache?
     *
     * @param stack the Stack in question
     * @return the slot
     */
    @Nullable
    public static EquipmentSlot getSlot(ItemStack stack) {
        ItemModule.ModuleInstance root = ItemModule.getModules(stack);
        Miapi.LOGGER.error("resolving Equipment Slot, if the item has this property");
        if (root != null) {
            JsonElement element = ItemModule.getMergedProperty(root, property, MergeType.OVERWRITE);
            if (element != null) {
                String name = element.getAsString();
                try{
                    return EquipmentSlot.byName(name);
                }
                catch (Exception ignored){

                }
            }
        }
        return null;
    }
}
