package smartin.miapi.client.gui.crafting.crafter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Managing class of the Modular Crafting Table
 */
@Environment(EnvType.CLIENT)
public class ModuleCrafter extends InteractAbleWidget {
    private ItemStack stack;
    private ItemModule module;
    private SlotProperty.ModuleSlot slot;
    private final Consumer<ItemStack> preview;
    private SlotProperty.ModuleSlot baseSlot = new SlotProperty.ModuleSlot(new ArrayList<>());
    private String paketIdentifier;
    private Inventory linkedInventory;
    CraftView craftView;
    Consumer<Slot> removeSlot;
    Consumer<Slot> addSlot;
    public CraftingScreenHandler handler;

    public ModuleCrafter(int x, int y, int width, int height, Consumer<SlotProperty.ModuleSlot> selected, Consumer<ItemStack> craftedItem, Inventory linkedInventory, Consumer<Slot> addSlot, Consumer<Slot> removeSlot) {
        super(x, y, width, height, Text.empty());
        this.linkedInventory = linkedInventory;
        this.preview = craftedItem;
        this.removeSlot = removeSlot;
        this.addSlot = addSlot;
        CraftView.currentSlots.forEach(removeSlot::accept);
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

    public void setMode(Mode mode) {
        if (craftView != null) {
            craftView.closeSlot();
            craftView = null;
        }
        if (mode == Mode.DETAIL && !(stack.getItem() instanceof ModularItem)) {
            Material material = MaterialProperty.getMaterial(stack);
            if (material != null) {
                mode = Mode.MATERIAL;
            }
        }
        switch (mode) {
            case DETAIL -> {
                this.children().clear();
                DetailView detailView = new DetailView(this.getX(), this.getY(), this.width, this.height, this.baseSlot, this.slot,
                        toEdit -> {
                            this.slot = toEdit;
                            setMode(Mode.EDIT);
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
                        });
                this.children.add(detailView);
            }
            case CRAFT -> {
                craftView = new CraftView(this.getX(), this.getY(), this.width, this.height, paketIdentifier, module, stack, linkedInventory, 1, slot, (backSlot) -> {
                    slot = backSlot;
                    setMode(Mode.REPLACE);
                }, (replaceItem) -> {
                    preview.accept(replaceItem);
                }, addSlot, removeSlot, handler);
                this.children().clear();
                this.addChild(craftView);
            }
            case EDIT -> {
                if (this.slot != null) {
                    ItemModule.ModuleInstance instance = slot.inSlot;
                    if (instance != null) {
                        EditView view = new EditView(this.getX(), this.getY(), this.width, this.height, stack, instance, (previewItem) -> {
                            preview.accept(previewItem);
                        }, (object) -> {
                            setMode(Mode.DETAIL);
                        });
                        this.children().clear();
                        this.addChild(view);
                    }
                }
            }
            case REPLACE -> {
                this.children.clear();
                ReplaceView view = new ReplaceView(this.getX(), this.getY(), this.width, this.height, slot, this::setSelectedSlot, (itemModule -> {
                    this.module = itemModule;
                    setMode(Mode.CRAFT);
                }), (itemModule -> {
                    CraftAction action = new CraftAction(stack, slot, itemModule, null, handler.blockEntity, new PacketByteBuf[0]);
                    action.linkInventory(linkedInventory, 1);
                    preview.accept(action.getPreview());
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
        }
        preview.accept(stack);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
    }

    public enum Mode {
        DETAIL,
        EDIT,
        REPLACE,
        CRAFT,
        MATERIAL
    }
}
