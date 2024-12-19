package smartin.miapi.modules.edit_options;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.crafter.glint.GlintEditView;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.network.Networking;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GlintEditOption implements EditOption {
    public static StreamCodec<ByteBuf, GlintProperty.RainbowGlintSettings> CODEC = ByteBufCodecs.fromCodec(GlintProperty.CODEC);

    @Override
    public ItemStack preview(FriendlyByteBuf buffer, EditContext editContext) {
        ModuleInstance moduleInstance = editContext.getInstance() == null ? ItemModule.getModules(editContext.getItemstack()) : editContext.getInstance();
        if (moduleInstance == null) {
            return editContext.getItemstack();
        }
        GlintProperty.RainbowGlintSettings settings = CODEC.decode(buffer);
        boolean remove = settings.colors.length == 0;
        if (settings.isItem) {
            moduleInstance.getRoot().allSubModules().forEach(module -> {
                ModuleDataPropertiesManager.setProperty(
                        module,
                        GlintProperty.property,
                        remove ? null : settings
                );
            });
        } else {
            ModuleDataPropertiesManager.setProperty(
                    moduleInstance,
                    GlintProperty.property,
                    remove ? null : settings
            );
        }
        ItemStack itemStack = editContext.getItemstack().copy();
        moduleInstance.getRoot().writeToItem(itemStack);
        return itemStack;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return VisualModularItem.isModularItem(editContext.getItemstack()) && editContext.getItemstack().hasFoil() &&
               (MiapiPermissions.hasPerm(editContext.getPlayer(), "supporter") || MiapiPermissions.hasPerm(editContext.getPlayer(), "glint"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        return new GlintEditView(x, y, width, height, editContext, (prev) -> {
            FriendlyByteBuf buffer = Networking.createBuffer();
            CODEC.encode(buffer, prev);
            editContext.preview(buffer);
        }, (craft) -> {
            FriendlyByteBuf buffer = Networking.createBuffer();
            CODEC.encode(buffer, craft);
            editContext.craft(buffer);
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25 + 196, 512, 512, "miapi.ui.edit_option.hover.glint", this);
    }
}
