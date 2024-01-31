package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class EmptyCraftingWidget extends InteractAbleWidget {
    private final DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public EmptyCraftingWidget(int x, int y, int width, int height, CraftAction action) {
        super(x, y, width, height, Text.empty());

        ModuleInstance moduleInstance = new ModuleInstance(action.toAdd);
        Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
        Text descriptionText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName() + ".description", moduleInstance);

        float headerScale = 1.5f;

        TransformableWidget headerHolder = new TransformableWidget(x, y, width, height, headerScale);
        addChild(headerHolder);


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale) + 2, (int) ((this.width - 10) / headerScale), displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        MultiLineTextWidget description = new MultiLineTextWidget(x + 5, y + 25, width - 10, height - 40, descriptionText);
        addChild(description);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();

        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 368, 138, 26, 26, getWidth(), getHeight(), 512, 512, 5);

        //drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX() + 50, getY() + allowedMaterial.slotHeight - 3, 367 - 28, 137, 28, 28, 20, 20, 512, 512, 5);

        super.render(drawContext, mouseX, mouseY, delta);
    }
}
