package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.crafting.moduleCrafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotDisplay.SlotDisplay;

public class CraftingGUI extends ParentHandledScreen<CraftingScreenHandler> implements ScreenHandlerProvider<CraftingScreenHandler> {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/crafting_gui_background.png");
    private final int rows;
    private PlayerInventory playerInventory;

    public CraftingGUI(CraftingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.playerInventory = playerInventory;
        this.rows = 6;
        this.backgroundWidth = 176;
        this.backgroundHeight = 114 + 6 * 18;
    }

    public void init(){
        SlotDisplay slotDisplay = new SlotDisplay(null,(this.width - this.backgroundWidth) / 2+5,(this.height - this.backgroundHeight) / 2+15,100,70);
        slotDisplay.setItem(playerInventory.getStack(0));
        this.addSelectableChild(slotDisplay);
        ModuleCrafter moduleCrafter = new ModuleCrafter((this.width - this.backgroundWidth) / 2+76,(this.height - this.backgroundHeight) / 2+60,96,88);
        moduleCrafter.setItem(playerInventory.getStack(0));
        moduleCrafter.registerCallBack(slotDisplay::select);
        slotDisplay.registerCallBack(moduleCrafter::select);
        this.addSelectableChild(moduleCrafter);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.drawBackground(matrices,delta, mouseX, mouseY);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.rows * 18 + 17);
        this.drawTexture(matrices, i, j + this.rows * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }
}
