package smartin.miapi.modules.edit_options;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Override
    public void renderIcon(GuiGraphics drawContext, boolean isSelected, int x, int y, float delta) {
        drawContext.blit(CraftingScreen.BACKGROUND_TEXTURE, x, y, 338, 89, 18, 18, 512, 512);
    }

    @Override
    public Component getHoverName() {
        return Component.translatable("miapi.ui.edit_option.hover.glint");
    }
}
