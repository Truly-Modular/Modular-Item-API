package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.registries.RegistryInventory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Edits allow you to edit a Module on an Itemstack.
 * EditOptions need to be registered at {@link RegistryInventory#editOptions}
 */
public interface EditOption {

    /**
     * This is the Logic that actually executes the Edit.
     * The Buffer is the on parsed into by the consumers of the {@link EditOption#getGui(int, int, int, int, ItemStack, ItemModule.ModuleInstance, Consumer, Consumer, Consumer)}
     *
     * @param buffer   the Buffer with the detail of the Edit
     * @param stack    the ItemStack pre-edit
     * @param instance the Selected ModuleInstance
     * @return the new ItemStack after the Edit
     */
    ItemStack execute(PacketByteBuf buffer, ItemStack stack, ItemModule.ModuleInstance instance);

    boolean isVisible(ItemStack stack, ItemModule.ModuleInstance instance);

    @Environment(EnvType.CLIENT)
    InteractAbleWidget getGui(int x, int y, int width, int height,ItemStack stack, ItemModule.ModuleInstance instance, Consumer<PacketByteBuf> craft, Consumer<PacketByteBuf> preview, Consumer<Objects> back);


}
