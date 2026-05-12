package smartin.miapi.modules.edit_options.CreateItemOption;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.client.gui.crafting.crafter.create_module.CreateListView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.EditOptionIcon;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreateItemOption implements EditOption {
    private static final String MODERN_MODE_KEY = "superior_miapi.modern_mode";
    private static final String INVENTORY_OFFSET_KEY = "superior_miapi.inventory_offset";
    private static final String RETURN_LEFTOVER_TO_INVENTORY_KEY = "superior_miapi.return_leftover_material_to_inventory";

    public static CreateItem selected;
    public static List<CreateItem> createAbleItems = new ArrayList<>();


    public CreateItemOption() {
    }

    public static void setup() {
        Miapi.registerReloadHandler(ReloadEvents.END, "create_options", (isClient -> {
            createAbleItems.clear();
        }), ((isClient, path, data) -> {
            if (isClient) {
                CreateItem createItem = Miapi.gson.fromJson(data, JsonCreateItem.class);
                createAbleItems.add(createItem);
                assert createItem.getItem() != null;
                assert createItem.getBaseModule() != null;
                assert createItem.getName() != null;
            }
        }), 0);
    }

    @Override
    public ItemStack preview(PacketByteBuf buffer, EditContext editContext) {
        String itemID = buffer.readString();
        String module = buffer.readString();
        int count = buffer.readInt();
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(itemID)));
        itemStack.setCount(count);
        ItemModule.ModuleInstance instance = new ItemModule.ModuleInstance(RegistryInventory.modules.get(module));
        instance.writeToItem(itemStack);
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        Inventory inventory = editContext.getLinkedInventory();
        if (
                PreviewManager.currentPreviewMaterial != null
        ) {
            inventory = new SimpleInventory(2);
            PreviewManager.currentPreviewMaterialStack.getDamage();
            inventory.setStack(1, PreviewManager.currentPreviewMaterialStack);
        }
        action.linkInventory(inventory, 1);
        action.setItem(itemStack);
        return action.getPreview();
    }

    @Override
    public ItemStack execute(PacketByteBuf buffer, EditContext editContext) {
        String itemID = buffer.readString();
        String module = buffer.readString();
        int count = buffer.readInt();
        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(itemID)));
        itemStack.setCount(count);
        ItemModule.ModuleInstance instance = new ItemModule.ModuleInstance(RegistryInventory.modules.get(module));
        instance.writeToItem(itemStack);
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(itemStack);
        action.linkInventory(editContext.getLinkedInventory(), this.resolveLinkedInventoryOffset(action, editContext));
        if (action.canPerform()) {
            ItemStack crafted = action.perform();
            this.returnModernMaterialRemainder(action, editContext);
            return crafted;
        } else {
            Miapi.LOGGER.warn("Could not previewStack Craft Action. This might indicate an exploit by " + editContext.getPlayer().getUuidAsString());
            return editContext.getItemstack();
        }
    }

    private void returnModernMaterialRemainder(final CraftAction action, final EditContext editContext) {
        if (action == null || action.data == null || editContext == null || editContext.getLinkedInventory() == null || editContext.getPlayer() == null) {
            return;
        }
        if (!Boolean.parseBoolean(action.data.getOrDefault(MODERN_MODE_KEY, "false"))
            || !Boolean.parseBoolean(action.data.getOrDefault(RETURN_LEFTOVER_TO_INVENTORY_KEY, "false"))) {
            return;
        }
        final String rawOffset = action.data.get(INVENTORY_OFFSET_KEY);
        if (rawOffset == null || rawOffset.isBlank()) {
            return;
        }
        final int offset;
        try {
            offset = Integer.parseInt(rawOffset.trim());
        } catch (NumberFormatException ignored) {
            return;
        }
        if (offset <= 0 || offset >= editContext.getLinkedInventory().size()) {
            return;
        }
        final ItemStack remainder = editContext.getLinkedInventory().getStack(offset);
        if (remainder.isEmpty()) {
            return;
        }
        final ItemStack toMove = remainder.copy();
        if (!editContext.getPlayer().getInventory().insertStack(toMove) && !toMove.isEmpty()) {
            editContext.getPlayer().dropItem(toMove, false);
        }
        editContext.getLinkedInventory().setStack(offset, ItemStack.EMPTY);
        editContext.getLinkedInventory().markDirty();
    }

    private int resolveLinkedInventoryOffset(final CraftAction action, final EditContext editContext) {
        if (action == null || action.data == null || editContext == null || editContext.getLinkedInventory() == null) {
            return 1;
        }
        if (!Boolean.parseBoolean(action.data.getOrDefault(MODERN_MODE_KEY, "false"))) {
            return 1;
        }
        final String rawOffset = action.data.get(INVENTORY_OFFSET_KEY);
        if (rawOffset == null || rawOffset.isBlank()) {
            return 1;
        }
        final int offset;
        try {
            offset = Integer.parseInt(rawOffset.trim());
        } catch (NumberFormatException ignored) {
            return 1;
        }
        return offset > 0 && offset < editContext.getLinkedInventory().size() ? offset : 1;
    }

    @Override
    public boolean isVisible(EditContext editContext) {
        return editContext.getItemstack().isEmpty();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext editContext) {
        PreviewManager.resetCursorStack();
        ReplaceOption.unsafeEditContext = editContext;
        return new CreateListView(x, y, width, height, editContext);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25 + 140, 512, 512, "miapi.ui.edit_option.hover.create", this);
    }

    @Environment(EnvType.CLIENT)
    public static EditContext transform(EditContext context, CreateItem item) {
        return new EditContext() {
            @Override
            public void craft(PacketByteBuf craftBuffer) {
                PacketByteBuf packetByteBuf = Networking.createBuffer();
                packetByteBuf.writeString(Registries.ITEM.getId(selected.getItem().getItem()).toString());
                packetByteBuf.writeString(selected.getBaseModule().getName());
                packetByteBuf.writeInt(selected.getItem().getCount());
                packetByteBuf.writeBytes(craftBuffer);
                context.craft(packetByteBuf);
            }

            @Override
            public void preview(PacketByteBuf preview) {
                PacketByteBuf packetByteBuf = Networking.createBuffer();
                packetByteBuf.writeString(Registries.ITEM.getId(selected.getItem().getItem()).toString());
                packetByteBuf.writeString(selected.getBaseModule().getName());
                packetByteBuf.writeInt(selected.getItem().getCount());
                packetByteBuf.writeBytes(preview);
                context.preview(packetByteBuf);
            }

            @Override
            public SlotProperty.ModuleSlot getSlot() {
                return new SlotProperty.ModuleSlot(new ArrayList<>());
            }

            @Override
            public ItemStack getItemstack() {
                ItemStack itemStack = item.getItem();
                getInstance().writeToItem(itemStack);
                return itemStack;
            }

            @Override
            public @Nullable ItemModule.ModuleInstance getInstance() {
                return new ItemModule.ModuleInstance(item.getBaseModule());
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

    public interface CreateItem {

        ItemStack getItem();

        ItemModule getBaseModule();

        Text getName();

        default boolean isAllowed(PlayerEntity player, ModularWorkBenchEntity entity) {
            return true;
        }

        double getPriority();
    }

    public static class JsonCreateItem implements CreateItem {
        public String item = "miapi:modular_item";
        public String module;
        public String translation;
        public int count = 1;
        public double priority = 0;

        @Override
        public ItemStack getItem() {
            ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(item)));
            itemStack.setCount(count);
            return itemStack;
        }

        @Override
        public ItemModule getBaseModule() {
            return RegistryInventory.modules.get(module);
        }

        @Override
        public Text getName() {
            return Text.translatable(translation);
        }

        public double getPriority() {
            return priority;
        }
    }
}
