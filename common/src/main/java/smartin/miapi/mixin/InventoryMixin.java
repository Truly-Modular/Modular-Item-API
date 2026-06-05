package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.modules.properties.inventory.ComponentBackedContainer;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.features.AutoPickupFeatureType;

@Mixin(Inventory.class)
public class InventoryMixin {


    @WrapMethod(
            method = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
    )
    private boolean miapi$pickupPrio(ItemStack stack, Operation<Boolean> original) {
        Inventory player = (Inventory) (Object) this;
        for (InventoryInstance instance : ItemInventoryManager.getInventoriesWith(player.player, AutoPickupFeatureType.FEATURE, f -> f).toList()) {
            if (instance.canInsert(stack)) {
                ComponentBackedContainer container = instance.create();
                for (int i = 0; i < instance.getSize(); i++) {
                    stack = container.addItem(stack);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return original.call(stack);
    }
}
