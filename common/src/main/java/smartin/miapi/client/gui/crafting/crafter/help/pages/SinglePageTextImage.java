package smartin.miapi.client.gui.crafting.crafter.help.pages;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ImageWidget;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;

public class SinglePageTextImage extends InteractAbleWidget {
    MultiLineTextWidget textWidget;
    ImageWidget imageWidget;

    public SinglePageTextImage(String text, String texture) {
        this(0, 0, Component.translatable(text), ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, texture));
    }

    public SinglePageTextImage(int x, int y, Component text, ResourceLocation texture) {
        super(x, y, 183, 161, text);
        textWidget = new MultiLineTextWidget(x + 4, y + 1, 182, 60, text);
        //366 200 183 100
        imageWidget = new ImageWidget(x + 2, y + 60, 183, 100, text, texture, 366, 200);
        this.addChild(textWidget);
        this.addChild(imageWidget);
    }

    @Override
    public void setX(int x) {
        textWidget.setX(x + 4);
        imageWidget.setX(x + 2);
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        textWidget.setY(y + 1);
        imageWidget.setY(y + 60);
        super.setY(y);
    }
}
