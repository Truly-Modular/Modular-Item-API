package smartin.miapi.item.modular;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.registries.RegistryInventory;

/**
 * Empty Interface to identify Modular Item
 */
public interface ModularItem extends VisualModularItem {

    public static DataComponentType<Boolean> IS_VISUAL_ONLY = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL).build();

    static int getDurability(ItemStack stack) {
        if (VisualModularItem.isVisualModularItem(stack)) {
            return DurabilityProperty.property.getValue(stack).orElse(1.0).intValue();
        }
        return stack.getMaxDamage();
    }

    static boolean isModularItemNoComponent(ItemStack itemStack) {
        return isModularItemNoComponent(itemStack.getItem()) && !(Boolean.TRUE.equals(itemStack.get(IS_VISUAL_ONLY)));
    }

    static boolean isModularItemNoComponent(Item item) {
        return item instanceof ModularItem;
    }

    static boolean isModularItem(ItemStack itemStack) {
        return isModularItem(itemStack, itemStack.getItem());
    }

    static boolean isModularItem(ItemStack itemStack, Item item) {
        if (item instanceof ModularItem) {
            ModuleInstance moduleInstance = itemStack.get(ModuleInstance.MODULE_INSTANCE_COMPONENT);
            return moduleInstance != null;
        }
        return false;
    }

    static @NotNull ItemStack convertToBroken(ItemStack current) {
        ItemStack broken = new ItemStack(RegistryInventory.brokenModualrItem);
        ItemModule.getModules(current).writeToItem(broken);
        broken.set(DataComponents.DAMAGE, current.get(DataComponents.DAMAGE));
        broken.set(DataComponents.MAX_DAMAGE, current.get(DataComponents.MAX_DAMAGE));
        broken.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        broken.set(IS_VISUAL_ONLY, true);
        current.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        current.set(IS_VISUAL_ONLY, true);
        return broken;
    }
}
