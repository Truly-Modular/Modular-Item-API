package smartin.miapi.modules.edit_options.skins.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ClickAbleTextWidget;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.skins.Skin;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SkinGui extends InteractAbleWidget {

    public final Consumer<PacketByteBuf> craft;
    public final Consumer<PacketByteBuf> preview;
    public String currentPreview;
    public ItemModule.ModuleInstance instance;

    @Environment(EnvType.CLIENT)
    public SkinGui(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance instance, Consumer<PacketByteBuf> craft, Consumer<PacketByteBuf> preview) {
        super(x, y, width, height, Text.empty());
        this.craft = craft;
        this.preview = preview;
        this.instance = instance;
        Map<String, Skin> maps = SkinOptions.skins.get(instance.module);
        if (maps == null) {
            maps = new HashMap<>();
        }
        List<InteractAbleWidget> widgets = new ArrayList<>();
        SkinTabGui parentSkinTab = new SkinTabGui(this, x, y + 30, width, "", maps);
        widgets.add(parentSkinTab);
        ScrollList list = new ScrollList(x, y + 30, width, height - 30, widgets);
        this.addChild(list);
        TextFieldWidget textFieldWidget = new ClickAbleTextWidget(MinecraftClient.getInstance().textRenderer, x + 2, y + 2, this.width - 4, 18, Text.literal("TITLE"));
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        textFieldWidget.setEditable(true);
        textFieldWidget.setVisible(true);
        textFieldWidget.setPlaceholder(Text.translatable(Miapi.MOD_ID + ".ui.search_placeholder"));
        textFieldWidget.setChangedListener(parentSkinTab::filter);
        this.addChild(textFieldWidget);
    }

    public String currentSkin() {
        if (instance != null) {
            return instance.moduleData.getOrDefault("skin", "");
        } else {
            Miapi.LOGGER.warn("instance null??????");
            return "";
        }
    }

    public void setPreview(String skinPath) {
        if (!skinPath.equals(currentPreview)) {
            currentPreview = skinPath;
            PacketByteBuf buf = Networking.createBuffer();
            buf.writeString(skinPath);
            preview.accept(buf);
        }
    }

    public void setCraft(String skinPath) {
        currentPreview = skinPath;
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeString(skinPath);
        craft.accept(buf);
    }

    interface SortAble {
        void filter(String search);

        String sortAndGetTop();

        boolean isActive();
    }

}
