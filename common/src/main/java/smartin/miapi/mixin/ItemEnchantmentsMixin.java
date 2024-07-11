package smartin.miapi.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;

import java.util.Set;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

    @Inject(method = "entrySet()Ljava/util/Set;", at = @At("RETURN"))
    public void miapi$adjustFakeEnchants(CallbackInfoReturnable<Set<Object2IntMap.Entry<Holder<Enchantment>>>> cir) {
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = FakeEnchantmentManager.lookupMap.get(itemEnchantments);
        if (itemStack != null && itemStack.getItem() instanceof ModularItem) {
            cir.setReturnValue(FakeEnchantmentManager.adjustEnchantments(cir.getReturnValue(), itemStack));
        }
    }

    @Inject(method = "getLevel", at = @At("RETURN"))
    public void miapi$adjustEnchantLevel(Holder<Enchantment> enchantment, CallbackInfoReturnable<Integer> cir) {
        ItemEnchantments itemEnchantments = (ItemEnchantments) (Object) this;
        ItemStack itemStack = FakeEnchantmentManager.lookupMap.get(itemEnchantments);
        if (itemStack != null && itemStack.getItem() instanceof ModularItem) {
            cir.setReturnValue(FakeEnchantmentManager.adjustLevel(enchantment, cir.getReturnValue(), itemStack));
        }
    }
}
