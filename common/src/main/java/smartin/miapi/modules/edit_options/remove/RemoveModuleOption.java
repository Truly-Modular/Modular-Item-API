package smartin.miapi.modules.edit_options.remove;

import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.blueprint.BlueprintComponent;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.MutableModuleInstance;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.registries.RegistryInventory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Removes the current module from an item stack.
 * Creates a ModularPart Item with a blueprint to add the item back.
 */
public class RemoveModuleOption implements EditOption {

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        ItemStack itemStack = editContext.getItemstack();
        ModuleInstance moduleInstance = editContext.getInstance();
        if (moduleInstance != null) {
            if (moduleInstance.getParent() != null) {
                MutableModuleInstance removing = moduleInstance.asMutable();
                MutableModuleInstance parent = removing.removeFromParent();
                removing.toRecord().getRoot().writeToItem(itemStack);
                ItemStack partStack = new ItemStack(RegistryInventory.visualOnlymodularItem);
                partStack.setCount(itemStack.getCount());
                parent.toRecord().writeToItem(partStack);
                return itemStack;
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack execute(FriendlyByteBuf buffer, EditContext editContext) {
        ItemStack itemStack = editContext.getItemstack();
        ModuleInstance moduleInstance = editContext.getInstance();
        if (moduleInstance != null) {
            if (moduleInstance.getParent() != null) {
                MutableModuleInstance removing = moduleInstance.asMutable();
                MutableModuleInstance parent = removing.removeFromParent();
                removing.toRecord().getRoot().writeToItem(itemStack);
                ItemStack partStack = new ItemStack(RegistryInventory.visualOnlymodularItem);
                partStack.setCount(itemStack.getCount());
                parent.toRecord().writeToItem(partStack);
                partStack.set(BlueprintComponent.BLUEPRINT_COMPONENT, new BlueprintComponent(
                        parent.toRecord(),
                        Either.left(Boolean.TRUE),
                        Optional.of(Component.translatable("miapi.module.part", parent.toRecord().getRoot().cache().getModuleName())),
                        Optional.empty()));
                editContext.getPlayer().addItem(partStack);
                return itemStack;
            }
        }
        return itemStack;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        //disable for now
        return editContext.getInstance() != null && editContext.getInstance().getParent() != null && false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return null;
    }
}
