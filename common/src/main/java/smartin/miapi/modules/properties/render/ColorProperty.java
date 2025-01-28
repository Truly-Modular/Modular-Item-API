package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class ColorProperty extends CodecProperty<String> {
    public static ResourceLocation KEY = Miapi.id("color");
    public static ColorProperty property;

    public ColorProperty() {
        super(Codec.STRING);
        property = this;
    }

    public static Color getColor(ItemStack itemStack, ModuleInstance moduleInstance) {
        Material material = MaterialProperty.getMaterial(moduleInstance);
        if (material != null && !material.canBeDyed()) {
            return Color.WHITE;
        }
        if (property.getData(moduleInstance).isPresent()) {
            return new Color(property.getData(moduleInstance).get());
        }
        if (itemStack.has(DataComponents.DYED_COLOR)) {
            return new Color(itemStack.get(DataComponents.DYED_COLOR).rgb());
        }
        return Color.WHITE;
    }

    public static boolean hasColor(ItemStack itemStack, ModuleInstance moduleInstance) {
        Material material = MaterialProperty.getMaterial(moduleInstance);
        if (material != null && !material.canBeDyed()) {
            return false;
        }
        if (itemStack.has(DataComponents.DYED_COLOR)) {
            return true;
        }
        if (property.getData(moduleInstance).isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
