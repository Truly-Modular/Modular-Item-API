package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.glint.GlintEditView;
import smartin.miapi.item.modular.VisualModularItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GlintEditOption implements EditOption {
    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        return editContext.getItemstack();
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return VisualModularItem.isModularItem(editContext.getItemstack());
    }

    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return new GlintEditView(x, y, width, height, editContext, (c) -> {

        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25, 512, 512, "miapi.ui.edit_option.hover.replace", this);
    }
}
