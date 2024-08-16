package smartin.miapi.modules.properties;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class RarityProperty extends CodecProperty<Rarity> implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("rarity");
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
            return switch (old) {
                case COMMON, UNCOMMON -> Rarity.RARE;
                case RARE -> Rarity.EPIC;
                default -> old;
            };
        }
    }

    @Override
    public Rarity merge(Rarity left, Rarity right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        Rarity rarity = getRarity(itemStack);
        itemStack.set(DataComponents.RARITY, rarity);
    }
}
