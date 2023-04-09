package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.crafting.moduleCrafterv1.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;

public class CraftingGUI extends ParentHandledScreen<CraftingScreenHandler> implements ScreenHandlerProvider<CraftingScreenHandler> {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/crafting_gui_background.png");
    private static final Identifier TESTBACKGROUND = new Identifier(Miapi.MOD_ID, "textures/test.png");
    private final int rows;
    private PlayerInventory playerInventory;
    private ItemStack stack;
    SlotDisplay slotDisplay;
    ModuleCrafter moduleCrafter;

    public CraftingGUI(CraftingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, Text.empty());
        this.playerInventory = playerInventory;
        this.rows = 6;
        this.backgroundWidth = 278;
        this.backgroundHeight = 221;
    }

    public void init(){
        moduleCrafter = new ModuleCrafter((this.width - this.backgroundWidth) / 2+109,(this.height - this.backgroundHeight) / 2+5,163,130,(selectedSlot)->{
            Miapi.LOGGER.error("selectedSlot" +selectedSlot);
            slotDisplay.select(selectedSlot);
        },(item)->{
            setItem(item);
        });
        slotDisplay = new SlotDisplay(stack,(this.width - this.backgroundWidth) / 2+8,(this.height - this.backgroundHeight) / 2+8,206-20,98,(selected)->{
            moduleCrafter.setSelectedSlot(selected);
        });
        slotDisplay.setItem(getItem());
        this.addSelectableChild(slotDisplay);
        moduleCrafter.setItem(getItem());
        this.addSelectableChild(moduleCrafter);
        super.init();
        playerInventoryTitleX = -10;
        playerInventoryTitleY = -10;

    }

    public ItemStack getItem(){
        return handler.inventory.getStack(0);
        //return playerInventory.getStack(0);
    }

    public void setItem(ItemStack stack){
        if(stack==null){
            stack = ItemStack.EMPTY;
        }
        handler.inventory.setStack(0,stack);
        //playerInventory.setStack(0,stack);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.drawBackground(matrices,delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableDepthTest();
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        RenderSystem.enableDepthTest();
        if(!getItem().equals(stack)){
            stack = getItem();
            setItem(handler.inventory.getStack(0));
            if(slotDisplay!=null){
                slotDisplay.setItem(stack);
            }
            if(moduleCrafter!=null){
                moduleCrafter.setItem(stack);
            }
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        drawTexture(matrices, i, j, 0, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
    }
}
