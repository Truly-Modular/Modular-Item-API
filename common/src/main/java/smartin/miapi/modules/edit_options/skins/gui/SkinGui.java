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
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.skins.Skin;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.network.Networking;

import java.util.*;
import java.util.function.Consumer;

public class SkinGui extends InteractAbleWidget {

    public final Consumer<PacketByteBuf> craft;
    public final Consumer<PacketByteBuf> preview;
    public final Consumer<Objects> back;
    public String currentPreview;
    public ItemModule.ModuleInstance instance;

    @Environment(EnvType.CLIENT)
    public SkinGui(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance instance, Consumer<PacketByteBuf> craft, Consumer<PacketByteBuf> preview, Consumer<Objects> back) {
        super(x, y, width, height, Text.empty());
        this.craft = craft;
        this.preview = preview;
        this.back = back;
        this.instance = instance;
        Map<String, Skin> maps = SkinOptions.skins.get(instance.module);
        if (maps == null) {
            maps = new HashMap<>();
        }
        List<InteractAbleWidget> widgets = new ArrayList<>();
        SkinTabGui parentSkinTab = new SkinTabGui(this, x, y + 30, width, "", maps);
        widgets.add(parentSkinTab);
        ScrollList list = new ScrollList(x, y + 30, width, height - 45, widgets);
        this.addChild(list);
        TextFieldWidget textFieldWidget = new ClickAbleTextWidget(MinecraftClient.getInstance().textRenderer, x, y, this.width, 20, Text.literal("TITLE"));
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        textFieldWidget.setEditable(true);
        textFieldWidget.setVisible(true);
        textFieldWidget.setPlaceholder(Text.translatable(Miapi.MOD_ID+".ui.search_placeholder"));
        textFieldWidget.setChangedListener(parentSkinTab::filter);
        this.addChild(textFieldWidget);
        SimpleButton<Objects> backButton = new SimpleButton<>(this.getX() + 10, this.getY() + this.height - 10, 45, 14, Text.translatable(Miapi.MOD_ID+".ui.back"), null, back);
        this.addChild(backButton);
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

    public void back() {
        back.accept(null);
    }

    interface SortAble {
        void filter(String search);

        String sortAndGetTop();

        boolean isActive();
    }

}
