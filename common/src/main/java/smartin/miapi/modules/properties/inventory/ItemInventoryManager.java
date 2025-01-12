package smartin.miapi.modules.properties.inventory;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemInventoryManager {
    public static final Map<ResourceLocation, Function<Player, SlotInfo>> PLAYER_TO_SLOT = new HashMap<>();
    public static final Map<ResourceLocation, Function<ItemStack, InventoryType>> CONTAINERS_FOR_ITEMSTACK = new HashMap<>();

    public static void setup() {
    }

    public Map<SlotInfo, List<InventoryType>> getInventoryForPlayer(Player player) {
        Map<SlotInfo, List<InventoryType>> map = new HashMap<>();
        PLAYER_TO_SLOT.forEach((id, function) -> {
            SlotInfo info = function.apply(player);
            if (!info.getStack().isEmpty()) {
                List<InventoryType> inventoryTypes = new ArrayList<>();
                CONTAINERS_FOR_ITEMSTACK.forEach((slotId, inventoryTypeFunction) -> {
                    InventoryType type = inventoryTypeFunction.apply(info.getStack());
                    if (type != null && type.getSize() > 0) {
                        inventoryTypes.add(type);
                    }
                });
                if (!inventoryTypes.isEmpty()) {
                    map.put(info, inventoryTypes);
                }
            }
        });
        return map;
    }

    public interface SlotInfo {
        ItemStack getStack();

        Color getColor();

        CompoundTag getName();

        void renderIcon(int x, int y, int width, int height);
    }

    public interface InventoryType {
        ItemContainerContents getContents();

        boolean canEnter(ItemStack itemStack);

        int getSize();

        void saveToItem();

        CompoundTag getName();
    }
}
