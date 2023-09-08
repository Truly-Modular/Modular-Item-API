package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits allow you to edit a Module on an Itemstack.
 * EditOptions need to be registered at {@link RegistryInventory#editOptions}
 */
public interface EditOption {
    /**
     * Executed for previewStack
     * @param buffer
     * @param editContext
     * @return
     */
    ItemStack preview(PacketByteBuf buffer, EditContext editContext);

    /**
     * Executed on the server for actual CraftAction, use this for stat increments and non result related stuff like ingredient consumption
     * @param buf
     * @param editContext
     * @return
     */
    default ItemStack execute(PacketByteBuf buf, EditContext editContext) {
        return preview(buf, editContext);
    }

    boolean isVisible(EditContext editContext);

    @Environment(EnvType.CLIENT)
    InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext);

    @Environment(EnvType.CLIENT)
    InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected);

    interface EditContext {
        void craft(PacketByteBuf craftBuffer);

        void preview(PacketByteBuf preview);

        SlotProperty.ModuleSlot getSlot();

        ItemStack getItemstack();

        @Nullable
        ItemModule.ModuleInstance getInstance();

        @Nullable
        PlayerEntity getPlayer();

        @Nullable
        ModularWorkBenchEntity getWorkbench();

        Inventory getLinkedInventory();

        CraftingScreenHandler getScreenHandler();

        @Environment(EnvType.CLIENT)
        default void addSlot(Slot slot){

        }

        @Environment(EnvType.CLIENT)
        default void removeSlot(Slot slot){

        }
    }
}
