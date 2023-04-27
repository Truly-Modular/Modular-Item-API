package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;

import javax.annotation.Nullable;

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
        //EquipmentSlot.byName(name);
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
