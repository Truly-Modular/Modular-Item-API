package smartin.miapi.client.gui.crafting.crafter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.help.HelpGuiInfo;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Managing class of the Modular Crafting Table
 */
@Environment(EnvType.CLIENT)
public class ModuleCrafter extends InteractAbleWidget {
    public ItemStack stack;
    public SlotProperty.ModuleSlot slot;
    private final Consumer<ItemStack> preview;
    private SlotProperty.ModuleSlot baseSlot = new SlotProperty.ModuleSlot(new ArrayList<>());
    private String paketIdentifier;
    private final Container linkedInventory;
    EditView editView;
    Consumer<Slot> removeSlot;
    Consumer<Slot> addSlot;
    public CraftingScreenHandler handler;
    EditOption editOption;
    Consumer<SlotProperty.ModuleSlot> selected;
    public EditOption.EditContext editContext;
    Mode currentMode = Mode.DETAIL;
    CraftOption craftOption;
    public String moduleType = "default";

    public ModuleCrafter(int x, int y, int width, int height, Consumer<SlotProperty.ModuleSlot> selected, Consumer<ItemStack> craftedItem, Container linkedInventory, Consumer<Slot> addSlot, Consumer<Slot> removeSlot) {
        super(x, y, width, height, Component.empty());
        this.selected = selected;
        this.linkedInventory = linkedInventory;
        this.preview = craftedItem;
        this.removeSlot = removeSlot;
        this.addSlot = addSlot;
    }

    public ModuleCrafter(int x, int y, int width, int height, ModuleCrafter other) {
        super(x, y, width, height, Component.empty());
        this.selected = other.selected;
        this.linkedInventory = other.linkedInventory;
        this.preview = other.preview;
        this.removeSlot = other.removeSlot;
        this.addSlot = other.addSlot;
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
    }

    public void setSelectedSlot(SlotProperty.ModuleSlot instance) {
        slot = instance;
        setMode(Mode.DETAIL);
    }

    public void setBaseSlot(SlotProperty.ModuleSlot instance) {
        baseSlot = instance;
    }

    public void setPacketIdentifier(String identifier) {
        paketIdentifier = identifier;
    }

    public void setEditMode(EditOption editOption, EditOption.EditContext editContext) {
        if (editOption == null) {
        } else {
            this.editOption = editOption;
            this.editContext = editContext;
            this.setMode(Mode.EDIT);
        }
    }

    public void setMode(Mode mode) {
        currentMode = mode;
        if (mode != Mode.EDIT && editView != null) {
            editView.clearSlots();
        }
        if (mode == Mode.DETAIL && !(stack.getItem() instanceof VisualModularItem)) {
            Material material = MaterialProperty.getMaterialFromIngredient(stack);
            if (material != null) {
                mode = Mode.MATERIAL;
            }
            if (stack.isEmpty()) {
                mode = Mode.HELP;
            }
        }
        switch (mode) {
            case DETAIL -> {
                this.children().clear();
                DetailView detailView = new DetailView(this.getX(), this.getY(), this.width, this.height, this.baseSlot, this.slot,
                        toEdit -> {
                            selected.accept(toEdit);
                        },
                        toReplace -> {
                            if (toReplace == null) {
                                List<String> allowed = new ArrayList<>();
                                allowed.add("");
                                allowed.add("melee");
                                toReplace = new SlotProperty.ModuleSlot(allowed);
                            }
                            slot = toReplace;
                            setMode(Mode.REPLACE);
                        }, moduleType);
                this.children.add(detailView);
            }
            case EDIT -> {
                editView = new EditView(this.getX(), this.getY(), this.width, this.height, stack, slot, (previewItem) -> {
                    if (currentMode == Mode.EDIT) {
                        preview.accept(previewItem);
                    }
                }, (object) -> {
                    setMode(Mode.DETAIL);
                });
                if (editOption != null) {
                    editView.setEditOption(editOption);
                }
                this.children().clear();
                this.addChild(editView);
            }
            case REPLACE -> {
                this.children.clear();
                ReplaceView view = new ReplaceView(this.getX(), this.getY(), this.width, this.height, slot, editContext, this::setSelectedSlot, option -> {
                    this.craftOption = option;
                    setMode(Mode.CRAFT);
                }, (option -> {
                    CraftAction action = new CraftAction(editContext.getItemstack(), editContext.getSlot(), option.module(), editContext.getPlayer(), editContext.getWorkbench(), option.data().get(),editContext.getScreenHandler());
                    action.setItem(editContext.getLinkedInventory().getItem(0));
                    action.linkInventory(editContext.getLinkedInventory(), 1);
                    editContext.preview(action.toPacket(Networking.createBuffer()));
                }));
                addChild(view);
            }
            case MATERIAL -> {
                this.children.clear();
                MaterialDetailView detailView = new MaterialDetailView(this.getX(), this.getY(), this.width, this.getHeight(), stack, (object) -> {
                    setMode(Mode.DETAIL);
                });
                this.addChild(detailView);
            }
            case HELP -> {
                this.children().clear();
                CraftingScreen craftingScreen = CraftingScreen.getInstance();
                this.addChild(new HelpGuiInfo(this.getX(), this.getY(), this.width, this.getHeight(), Component.literal("miapi.help.helper"), (toFocus) -> {
                    craftingScreen.hoverElement = toFocus;
                }, (toRemove) -> {
                    craftingScreen.hoverElement = null;
                }));
            }
        }
        //preview.accept(stack);
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderWidget(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public enum Mode {
        DETAIL,
        EDIT,
        REPLACE,
        CRAFT,
        MATERIAL,
        HELP
    }
}
