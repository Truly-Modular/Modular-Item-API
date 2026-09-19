package smartin.miapi.client.gui.crafting;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public interface CraftingHandler {
    List<Slot> getActiveSlots();

    void addSlotByClient(Slot slot);

    void removeSlotByClient(Slot slot);

    Container getAttachedStorage();

    Player getCurrentPlayer();

    void removeSlotListener(ContainerListener listener);

    void addSlotListener(ContainerListener listener);

    void sendEditPacket(FriendlyByteBuf buf);
}
