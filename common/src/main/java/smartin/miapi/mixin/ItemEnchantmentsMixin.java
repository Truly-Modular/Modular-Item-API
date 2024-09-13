package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;

import java.util.Set;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

    @ModifyReturnValue(method = "entrySet()Ljava/util/Set;", at = @At("RETURN"))
    public Set<Object2IntMap.Entry<Holder<Enchantment>>> miapi$adjustFakeEnchants(Set<Object2IntMap.Entry<Holder<Enchantment>>> original) {
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = FakeEnchantmentManager.lookupMap.get(itemEnchantments);
        if (itemStack != null && ModularItem.isModularItem(itemStack)) {
            //TODO:this might not work
            return FakeEnchantmentManager.adjustEnchantments(original, itemStack);
        }
        return original;
    }

    @ModifyReturnValue(method = "getLevel", at = @At("RETURN"))
    public int miapi$adjustEnchantLevel(int original, Holder<Enchantment> enchantment) {
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = FakeEnchantmentManager.lookupMap.get(itemEnchantments);
        if (itemStack != null && ModularItem.isModularItem(itemStack)) {
            //TODO:this might not work
            return FakeEnchantmentManager.adjustLevel(enchantment, original, itemStack);
        }
        return original;
    }
}
