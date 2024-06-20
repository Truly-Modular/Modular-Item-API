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
import smartin.miapi.client.gui.PreviewManager;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.client.gui.crafting.crafter.create_module.CreateListView;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
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
    public static CreateItem selected;
    public static List<CreateItem> createAbleItems = new ArrayList<>();


    public CreateItemOption() {
        Miapi.registerReloadHandler(ReloadEvents.END, "create_options", (isClient -> {
            createAbleItems.clear();
        }), ((isClient, path, data) -> {
            if(isClient){
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
        ModuleInstance instance = new ModuleInstance(RegistryInventory.modules.get(module));
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
        ModuleInstance instance = new ModuleInstance(RegistryInventory.modules.get(module));
        instance.writeToItem(itemStack);
        CraftAction action = new CraftAction(buffer, editContext.getWorkbench());
        action.setItem(itemStack);
        action.linkInventory(editContext.getLinkedInventory(), 1);
        if (action.canPerform()) {
            return action.perform();
        } else {
            Miapi.LOGGER.warn("Could not previewStack Craft Action. This might indicate an exploit by " + editContext.getPlayer().getUuidAsString());
            return editContext.getItemstack();
        }
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
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339 + 32, 25+140, 512, 512,"miapi.ui.edit_option.hover.create", this);
    }

    @Environment(EnvType.CLIENT)
    public static EditContext transform(EditContext context, CreateItem item) {
        return new EditContext() {
            @Override
            public void craft(PacketByteBuf craftBuffer) {
                PacketByteBuf packetByteBuf = Networking.createBuffer();
                packetByteBuf.writeString(Registries.ITEM.getId(selected.getItem().getItem()).toString());
                packetByteBuf.writeString(selected.getBaseModule().name());
                packetByteBuf.writeInt(selected.getItem().getCount());
                packetByteBuf.writeBytes(craftBuffer);
                context.craft(packetByteBuf);
            }

            @Override
            public void preview(PacketByteBuf preview) {
                PacketByteBuf packetByteBuf = Networking.createBuffer();
                packetByteBuf.writeString(Registries.ITEM.getId(selected.getItem().getItem()).toString());
                packetByteBuf.writeString(selected.getBaseModule().name());
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
            public @Nullable ModuleInstance getInstance() {
                return new ModuleInstance(item.getBaseModule());
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
    }

    public static class JsonCreateItem implements CreateItem {
        public String item = "miapi:modular_item";
        public String module;
        public String translation;
        public int count = 1;

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
    }
}
