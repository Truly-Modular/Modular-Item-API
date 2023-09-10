package smartin.miapi.client.gui.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ParentHandledScreen;
import smartin.miapi.client.gui.SimpleScreenHandlerListener;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.slotdisplay.SmithDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatDisplay;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

public class CraftingScreen extends ParentHandledScreen<CraftingScreenHandler> implements ScreenHandlerProvider<CraftingScreenHandler> {

    public static final Identifier BACKGROUND_TEXTURE = new Identifier(Miapi.MOD_ID, "textures/block/gui/crafter/background.png");
    private ItemStack stack;
    private ModuleCrafter moduleCrafter;
    private StatDisplay statDisplay;
    private SlotDisplay slotDisplay;
    private SmithDisplay smithDisplay;
    private ViewMinimizeButton minimizer;
    private SlotProperty.ModuleSlot baseSlot;
    @Nullable
    public SlotProperty.ModuleSlot slot;
    @Nullable
    private EditOption editOption;
    private TransformableWidget editHolder;
    static int editSpace = 30;

    List<InteractAbleWidget> editOptionIcons = new ArrayList<>();

    public CraftingScreen(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.empty());
        this.backgroundWidth = 393;
        this.backgroundHeight = 223;
    }

    public EditOption getEditOption() {
        return editOption;
    }

    public void selectSlot(SlotProperty.ModuleSlot slot) {
        this.slot = slot;
        updateEditOptions();
    }

    public void previewStack(ItemStack itemStack) {
        slotDisplay.setItem(itemStack);
        statDisplay.setCompareTo(itemStack);
        smithDisplay.setPreview(itemStack);
    }

    public void selectEditOption(EditOption editOption) {
        System.out.println("yes, edit option selection was called:");
        this.editOption = editOption;
        moduleCrafter.setSelectedSlot(slot);
        moduleCrafter.setEditMode(editOption, get(editOption));
    }

    public void init() {
        List<String> allowedModules = new ArrayList<>();
        allowedModules.add("melee");
        baseSlot = new SlotProperty.ModuleSlot(allowedModules);

        int centerX = (this.width - this.backgroundWidth) / 2;
        int centerY = (this.height - this.backgroundHeight) / 2;

        moduleCrafter = new ModuleCrafter(centerX + 51, centerY + 22, 144, 89, this::selectSlot, (item) -> {
            slotDisplay.setItem(item);
            statDisplay.setCompareTo(item);
            smithDisplay.setPreview(item);
        }, handler.inventory,
                handler::addSlotByClient, handler::removeSlotByClient);
        moduleCrafter.setPacketIdentifier(handler.packetID);
        this.addChild(moduleCrafter);

        slotDisplay = new SlotDisplay(stack, centerX + 51, centerY + 117, 70, 87, (selected) -> {

        });
        slotDisplay.setItem(getItem());
        this.addChild(slotDisplay);
        smithDisplay = new SmithDisplay(centerX + 140, centerY + 117, 55, 70);
        this.addChild(smithDisplay);
        statDisplay = new StatDisplay(centerX + 213, centerY + 30, 161, 95);
        this.addChild(statDisplay);

        minimizer = new ViewMinimizeButton(centerX + 180, centerY + 188, 18, 18,
                () -> moduleCrafter, crafter -> {
            removeChild(moduleCrafter);
            moduleCrafter = crafter;
            moduleCrafter.handler = handler;
            moduleCrafter.setPacketIdentifier(handler.packetID);
            addChild(moduleCrafter);
            EditOption op = getEditOption();
            updateItem(getItem());
            selectEditOption(op);
        },
                () -> slotDisplay,
                () -> smithDisplay,
                this::remove,
                this::addChild);
        this.addChild(minimizer);

        super.init();
        playerInventoryTitleX = -1000;
        playerInventoryTitleY = -1000;

        editHolder = new TransformableWidget(x, y, 0, 0, Text.empty());
        addChild(editHolder);

        updateItem(handler.inventory.getStack(0));
        if (moduleCrafter != null) {
            moduleCrafter.handler = handler;
        }
        this.handler.addListener(new SimpleScreenHandlerListener((handler, slotId, stack) -> {
            if (slotId == 36) {
                updateItem(stack);
            }
        }));

        addChild(new EditOptionIcon(moduleCrafter.getX() - 36, moduleCrafter.getY() + 4, 32, 28, this::selectEditOption, this::getEditOption, BACKGROUND_TEXTURE, 339, 25, 512, 512, null));

    }

    public ItemStack getItem() {
        return handler.inventory.getStack(0);
    }

    public void setItem(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        slotDisplay.setItem(stack);
        handler.inventory.setStack(0, stack);
    }

    private void updateItem(ItemStack stack) {
        slotDisplay.setItem(stack);
        ItemStack converted = ModularItemStackConverter.getModularVersion(stack);

        baseSlot.inSlot = ItemModule.getModules(converted);
        SlotProperty.ModuleSlot current = baseSlot;
        if (baseSlot.inSlot.module.equals(ItemModule.empty)) {
            current = null;
        }
        if (slotDisplay != null) {
            slotDisplay.setBaseSlot(current);
            slotDisplay.select(current);
            slotDisplay.setItem(converted);
        }
        if (smithDisplay != null) {
            smithDisplay.setPreview(converted);
        }
        if (moduleCrafter != null) {
            moduleCrafter.setBaseSlot(current);
            moduleCrafter.setItem(converted);
            moduleCrafter.setSelectedSlot(null);
        }
        if (statDisplay != null) {
            statDisplay.setOriginal(converted);
            statDisplay.setCompareTo(converted);
        }
        List<Integer> slotPos = new ArrayList<>();
        if (slot != null) {
            if (slot.inSlot != null) {
                slot.inSlot.calculatePosition(slotPos);
            } else if (slot.parent != null) {
                slot.parent.getPosition(slotPos);
            }
        }
        if (baseSlot.inSlot != null) {
            slot = SlotProperty.getSlotIn(baseSlot.inSlot.getRoot().getPosition(slotPos));
        } else {
            slot = null;
        }
        updateEditOptions();
    }

    public void updateEditOptions() {
        int x = moduleCrafter.getX() - 36;
        int y = moduleCrafter.getY() + 4 + editSpace;

        for (InteractAbleWidget widget : editOptionIcons) {
            editHolder.removeChild(widget);
        }
        editHolder.children().clear();

        for (EditOption option : RegistryInventory.editOptions.getFlatMap().values()) {
            EditOption.EditContext context = get(option);
            if (option.isVisible(context)) {
                InteractAbleWidget widget = option.getIconGui(x, y, 32, 28, this::selectEditOption, this::getEditOption);
                y += editSpace;
                editHolder.addChild(widget);
                editOptionIcons.add(widget);
            }
        }
        if (!editHolder.children().contains(editOption)) {
            editOption = null;
        }
        selectEditOption(editOption);
    }

    public EditOption.EditContext get(EditOption editOption) {
        return new EditOption.EditContext() {
            @Override
            public void craft(PacketByteBuf craftBuffer) {
                if (editOption != null) {
                    editOption.execute(craftBuffer, this);
                }
            }

            @Override
            public void preview(PacketByteBuf preview) {
                if (editOption != null) {

                    previewStack(editOption.preview(preview, this));
                }
            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItemstack() {
                return moduleCrafter.stack;
            }

            @Override
            public @Nullable ItemModule.ModuleInstance getInstance() {
                if (getSlot() == null) {
                    return null;
                }
                return getSlot().inSlot;
            }

            @Override
            public @Nullable PlayerEntity getPlayer() {
                return MinecraftClient.getInstance().player;
            }

            @Override
            public @Nullable ModularWorkBenchEntity getWorkbench() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.blockEntity;
                }
                return null;
            }

            @Override
            public Inventory getLinkedInventory() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler.inventory;
                }
                return null;
            }

            @Override
            public CraftingScreenHandler getScreenHandler() {
                if (MinecraftClient.getInstance().player.currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
                    return craftingScreenHandler;
                }
                return null;
            }
        };
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        //(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
        drawContext.drawTexture(BACKGROUND_TEXTURE, i + 43, j + 14, 338, 199, 0.0f, 0.0f, 338, 199, 512, 512);
        super.render(drawContext, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(drawContext, mouseX, mouseY);
        this.renderHover(drawContext, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

    }
}
