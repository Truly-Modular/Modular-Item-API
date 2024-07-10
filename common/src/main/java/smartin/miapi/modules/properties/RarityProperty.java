package smartin.miapi.modules.properties;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class RarityProperty extends CodecBasedProperty<Rarity> implements ComponentApplyProperty {
    public static String KEY = "rarity";
    public static RarityProperty property;

    public RarityProperty() {
        super(Rarity.CODEC);
        property = this;
    }

    public static Rarity getRarity(ItemStack itemStack) {
        return applyEnchant(itemStack, property.getData(itemStack).orElse(Rarity.COMMON));
    }


    private static Rarity applyEnchant(ItemStack itemStack, Rarity old) {
        if (!itemStack.isEnchanted()) {
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

    @Override
    public Rarity merge(Rarity left, Rarity right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    @Override
    public void updateComponent(ItemStack itemStack) {
        Rarity rarity = getRarity(itemStack);
        itemStack.set(DataComponents.RARITY, rarity);
    }
}
