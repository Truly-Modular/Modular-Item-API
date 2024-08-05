package smartin.miapi.modules.edit_options.skins.gui;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ClickAbleTextWidget;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.edit_options.skins.Skin;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SkinGui extends InteractAbleWidget {

    public final Consumer<FriendlyByteBuf> craft;
    public final Consumer<FriendlyByteBuf> preview;
    public String currentPreview;
    public ModuleInstance instance;

    @Environment(EnvType.CLIENT)
    public SkinGui(int x, int y, int width, int height, ItemStack stack, ModuleInstance instance, Consumer<FriendlyByteBuf> craft, Consumer<FriendlyByteBuf> preview) {
        super(x, y, width, height, Component.empty());
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
        EditBox textFieldWidget = new ClickAbleTextWidget(Minecraft.getInstance().font, x + 2, y + 2, this.width - 4, 18, Component.literal("TITLE"));
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        textFieldWidget.setEditable(true);
        textFieldWidget.setVisible(true);
        textFieldWidget.setHint(Component.translatable(Miapi.MOD_ID + ".ui.search_placeholder"));
        textFieldWidget.setResponder(parentSkinTab::filter);
        this.addChild(textFieldWidget);
    }

    public String currentSkin() {
        if (instance != null) {
            return instance.moduleData.getOrDefault("skin", new JsonObject()).getAsString();
        } else {
            Miapi.LOGGER.warn("instance null??????");
            return "";
        }
    }

    public void setPreview(String skinPath) {
        if (!skinPath.equals(currentPreview)) {
            currentPreview = skinPath;
            FriendlyByteBuf buf = Networking.createBuffer();
            buf.writeUtf(skinPath);
            preview.accept(buf);
        }
    }

    public void setCraft(String skinPath) {
        currentPreview = skinPath;
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeUtf(skinPath);
        craft.accept(buf);
    }

    interface SortAble {
        void filter(String search);

        String sortAndGetTop();

        boolean isActive();
    }

}
