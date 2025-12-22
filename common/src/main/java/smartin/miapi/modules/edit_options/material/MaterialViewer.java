package smartin.miapi.modules.edit_options.material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MaterialViewer implements EditOption {
    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        return editContext.getItemstack();
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return (editContext.getItemstack()==null || editContext.getItemstack().isEmpty());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return new MaterialViewerWidget(x,y,width,height);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339, 25 + 38 + 56 * 3,  512, 512, "miapi.ui.edit_option.hover.material", this);
    }
}
