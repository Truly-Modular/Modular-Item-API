package smartin.miapi.client.gui.crafting.crafter.replace;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.*;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.material.AllowedMaterial;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Environment(EnvType.CLIENT)
public class MaterialCraftingWidget extends InteractAbleWidget {
    private final AllowedMaterial allowedMaterial;
    private final ScrollingTextWidget costDescr;
    public CraftAction action;
    private final DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public MaterialCraftingWidget(AllowedMaterial allowedMaterial, int x, int y, int width, int height, CraftAction action) {
        super(x, y, width, height, Text.empty());
        this.action = action;
        this.allowedMaterial = allowedMaterial;
        allowedMaterial.slotHeight = height + 12;

        ModuleInstance moduleInstance = new ModuleInstance(action.toAdd);
        Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.name(), moduleInstance);
        Text descriptionText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.name() + ".description", moduleInstance);

        float headerScale = 1.5f;

        TransformableWidget headerHolder = new TransformableWidget(x, y, width, height, headerScale);
        addChild(headerHolder);


        ScrollingTextWidget header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale) + 2, (int) ((this.width - 10) / headerScale), displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        headerHolder.addChild(header);
        MultiLineTextWidget description = new MultiLineTextWidget(x + 5, y + 25, width - 10, height - 40, descriptionText);
        costDescr = new ScrollingTextWidget(x + 71, y + this.height - 8, 78, Text.empty());
        //costDescr.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
        costDescr.textColor = ColorHelper.Argb.getArgb(255, 225, 225, 225);
        this.addChild(new HoverMaterialList(action.toAdd, x + 71, y + this.height + 10, 31, 19));
        addChild(description);
        addChild(costDescr);
    }

    @Override
    public void renderWidget(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        ReplaceOption.unsafeCraftAction = action;
        if (
                ReplaceOption.unsafeEditContext != null &&
                !ReplaceOption.unsafeEditContext.getScreenHandler().inventory.getStack(1).isEmpty()) {
            PreviewManager.resetCursorStack();

        }


        int textureOffset = 0;

        if (allowedMaterial.materialCostClient < allowedMaterial.materialRequirementClient) {
            costDescr.textColor = ColorHelper.Argb.getArgb(255, 225, 225, 125);
        } else {
            costDescr.textColor = ColorHelper.Argb.getArgb(255, 125, 225, 125);
        }

        costDescr.setText(Text.literal(modifierFormat.format(allowedMaterial.materialCostClient) + "/" + modifierFormat.format(allowedMaterial.materialRequirementClient)));

        costDescr.setY(this.getY() + allowedMaterial.slotHeight + 10);

        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX(), getY(), 368, 138, 26, 26, getWidth(), getHeight(), 512, 512, 5);

        drawTextureWithEdge(drawContext, CraftingScreen.BACKGROUND_TEXTURE, getX() + 50, getY() + allowedMaterial.slotHeight - 3, 367 - 28, 137, 28, 28, 20, 20, 512, 512, 5);

        super.render(drawContext, mouseX, mouseY, delta);
    }
}
