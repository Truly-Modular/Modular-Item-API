package smartin.miapi.modules.edit_options;

import smartin.miapi.client.gui.InteractAbleWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ModuleMergeEditOption implements EditOption {
    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        return null;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return false;
    }

    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return null;
    }

    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return null;
    }
}
