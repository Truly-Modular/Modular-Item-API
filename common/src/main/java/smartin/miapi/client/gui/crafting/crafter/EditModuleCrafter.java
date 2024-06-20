package smartin.miapi.client.gui.crafting.crafter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Managing class of the Modular Crafting Table
 */
@Environment(EnvType.CLIENT)
public class EditModuleCrafter extends InteractAbleWidget {
    public ItemStack stack;
    public SlotProperty.ModuleSlot slot;
    private SlotProperty.ModuleSlot baseSlot = new SlotProperty.ModuleSlot(new ArrayList<>());
    Consumer<SlotProperty.ModuleSlot> selected;
    EditView editView;
    public CraftingScreenHandler handler;
    EditOption editOption;
    public EditOption.EditContext editContext;
    Mode currentMode = Mode.DETAIL;
    CraftOption craftOption;
    public String moduleType;

    public EditModuleCrafter(int x, int y, int width, int height, EditOption editOption, EditOption.EditContext editContext, String moduleType) {
        super(x, y, width, height, Text.empty());
        this.moduleType = moduleType;
        this.editOption = editOption;
        this.editContext = editContext;
        setMode(Mode.DETAIL);
    }

    public void setMode(Mode mode) {
        currentMode = mode;
        switch (mode) {
            case DETAIL -> {
                this.children().clear();
                DetailView detailView = new DetailView(this.getX(), this.getY(), this.width, this.height, editContext.getSlot(), editContext.getSlot(),
                        toEdit -> {
                            if (toEdit == null) {
                                List<String> allowed = new ArrayList<>();
                                allowed.add("");
                                allowed.add("melee");
                                toEdit = new SlotProperty.ModuleSlot(allowed);
                            }
                            slot = toEdit;
                            setMode(Mode.REPLACE);
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
            case REPLACE -> {
                this.children.clear();
                CraftEditOption craftEditOption = new CraftEditOption(this.getX(), this.getY(), this.width, this.height, transform(editContext,slot));
                addChild(craftEditOption);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static EditOption.EditContext transform(EditOption.EditContext context, SlotProperty.ModuleSlot slot) {
        return new EditOption.EditContext() {
            @Override
            public void craft(PacketByteBuf craftBuffer) {
                context.craft(craftBuffer);
            }

            @Override
            public void preview(PacketByteBuf preview) {
                context.preview(preview);
            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return slot;
            }

            @Override
            public ItemStack getItemstack() {
                return context.getItemstack();
            }

            @Override
            public @Nullable ItemModule.ModuleInstance getInstance() {
                return context.getInstance();
            }

            @Override
            public @Nullable PlayerEntity getPlayer() {
                return context.getPlayer();
            }

            @Override
            public @Nullable ModularWorkBenchEntity getWorkbench() {
                return context.getWorkbench();
            }

            @Override
            public Inventory getLinkedInventory() {
                return context.getLinkedInventory();
            }

            @Override
            public CraftingScreenHandler getScreenHandler() {
                return context.getScreenHandler();
            }

            @Environment(EnvType.CLIENT)
            public void addSlot(Slot slot) {
                context.addSlot(slot);
            }

            @Environment(EnvType.CLIENT)
            public void removeSlot(Slot slot) {
                context.removeSlot(slot);
            }
        };
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
    }

    public enum Mode {
        REPLACE,
        DETAIL
    }
}
