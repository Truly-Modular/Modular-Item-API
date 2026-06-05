package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.ComponentBackedContainer;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class AutoPickupFeatureType implements InventoryFeatureType<Boolean> {
    public static final AutoPickupFeatureType FEATURE = new AutoPickupFeatureType();

    private AutoPickupFeatureType() {

    }

    public static final ResourceLocation ID =
            Miapi.id("auto_pickup");

    public static @Nullable ItemStack tryPickUpBeforeInventory(ItemStack stack, Player player) {
                for (InventoryInstance instance : ItemInventoryManager.getInventoriesWith(player, FEATURE, f -> f).toList()) {
                    if (instance.canInsert(stack)) {
                        ComponentBackedContainer container = instance.create();
                        for (int i = 0; i < instance.getSize(); i++) {
                            stack = container.addItem(stack);
                            if (stack.isEmpty()) {
                                return null;
                            }
                        }
                    }
                }
                return stack;
            }

    @Override
    public Boolean merge(Boolean left, Boolean right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }

    @Override
    public Boolean initialize(Boolean property, ModuleInstance context) {
        return property;
    }
}