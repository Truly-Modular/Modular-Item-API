package smartin.miapi.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.MixinContextFlags;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;
import smartin.miapi.modules.properties.enchanment.ItemEnchantmentsAccessor;

import java.util.Set;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin implements ItemEnchantmentsAccessor {

    @Unique
    ItemStack ownerStack;

    @Override
    public ItemStack getOwnerStack() {
        return ownerStack;
    }

    @Override
    public void setOwnerStack(ItemStack itemStack) {
        this.ownerStack = itemStack;
    }

    @ModifyReturnValue(method = "entrySet()Ljava/util/Set;", at = @At("RETURN"))
    public Set<Object2IntMap.Entry<Holder<Enchantment>>> miapi$adjustFakeEnchants(Set<Object2IntMap.Entry<Holder<Enchantment>>> original) {
        if (MixinContextFlags.CALLED_FROM_MUTABLE.get()) {
            return original;
        }
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = getOwnerStack();
        if (itemStack != null && ModularItem.isModularItem(itemStack)) {
            //TODO:this might not work
            return FakeEnchantmentManager.adjustEnchantments(original, itemStack);
        }
        return original;
    }

    @ModifyReturnValue(method = "getLevel", at = @At("RETURN"))
    public int miapi$adjustEnchantLevel(int original, Holder<Enchantment> enchantment) {
        if (MixinContextFlags.CALLED_FROM_MUTABLE.get()) {
            return original;
        }
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = getOwnerStack();
        if (itemStack != null && ModularItem.isModularItem(itemStack)) {
            //TODO:this might not work
            return FakeEnchantmentManager.adjustLevel(enchantment, original, itemStack);
        }
        return original;
    }
}
