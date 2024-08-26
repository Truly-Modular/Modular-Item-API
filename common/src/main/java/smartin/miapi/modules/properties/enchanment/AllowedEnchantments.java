package smartin.miapi.modules.properties.enchanment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Optional;

public class AllowedEnchantments extends CodecProperty<AllowedEnchantments.AllowedEnchantsData> {
    public AllowedEnchantments() {
        super(null);
        EnchantmentHelper helper;
    }

    public static boolean isPrimaryAllowed(ItemStack itemStack, Enchantment enchantment, boolean oldValue) {
        return oldValue;
    }

    public static boolean isAllowed(ItemStack itemStack, Enchantment enchantment, boolean oldValue) {
        return oldValue;
    }

    @Override
    public AllowedEnchantsData merge(AllowedEnchantsData left, AllowedEnchantsData right, MergeType mergeType) {
        return new AllowedEnchantsData(
                ModuleProperty.mergeList(left.allowed(), right.allowed(), mergeType),
                ModuleProperty.mergeList(left.forbidden(), right.forbidden(), mergeType)
        );
    }

    public static record AllowedEnchantsData(List<ResourceLocation> allowed, List<ResourceLocation> forbidden) {

        Optional<Boolean> isAllowed(Enchantment enchantment) {

            return Optional.empty();
        }

        public static boolean isOf(Enchantment enchantment, List<ResourceLocation> ids) {

            return false;
        }
    }
}
