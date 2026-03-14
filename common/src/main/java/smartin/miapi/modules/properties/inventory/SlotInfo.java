package smartin.miapi.modules.properties.inventory;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * This interface allows different Inventory sources.
 * f.e. when call {@link  ItemInventoryManager#PLAYER_TO_SLOT} is called this should resolve to items like the different armor slots.
 * Any Slot that may provide a partInventory needs to be implemented with this and registered at{@link  ItemInventoryManager#PLAYER_TO_SLOT}
 */
public interface SlotInfo {
    ItemStack getStack(Player player);

    void setStack(ItemStack saved, Player player);

    ResourceLocation getID();

    Color getColor();

    Component getName();

    double priority();

    void renderIcon(int x, int y, int width, int height);
}
