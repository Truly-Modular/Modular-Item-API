package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.modules.ModuleInstance;
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
    ItemStack preview(FriendlyByteBuf buffer, EditContext editContext);

    /**
     * Executed on the server for actual CraftAction, use this for stat increments and non result related stuff like ingredient consumption
     * @param buf
     * @param editContext
     * @return
     */
    default ItemStack execute(FriendlyByteBuf buf, EditContext editContext) {
        return preview(buf, editContext);
    }

    boolean isVisible(EditContext editContext);

    @Environment(EnvType.CLIENT)
    InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext);

    @Environment(EnvType.CLIENT)
    InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected);

    interface EditContext {
        void craft(FriendlyByteBuf craftBuffer);

        void preview(FriendlyByteBuf preview);

        SlotProperty.ModuleSlot getSlot();

        ItemStack getItemstack();

        @Nullable
        ModuleInstance getInstance();

        @Nullable
        Player getPlayer();

        @Nullable
        ModularWorkBenchEntity getWorkbench();

        Container getLinkedInventory();

        CraftingScreenHandler getScreenHandler();

        @Environment(EnvType.CLIENT)
        default void addSlot(Slot slot){

        }

        @Environment(EnvType.CLIENT)
        default void removeSlot(Slot slot){

        }
    }
}
