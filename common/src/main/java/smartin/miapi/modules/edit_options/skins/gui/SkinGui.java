package smartin.miapi.modules.edit_options.skins.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
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
        this.addChild(new SkinTabGui(this, x, y, width, "", maps));
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
    }

}
