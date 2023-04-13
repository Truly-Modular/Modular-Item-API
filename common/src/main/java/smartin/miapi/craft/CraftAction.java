package smartin.miapi.craft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftAction {
    private final ItemModule toAdd;
    private final PlayerEntity player;
    private final List<Integer> slotId = new ArrayList<>();
    private ItemStack old;
    private Inventory linkedInventory;
    private int inventoryOffset;

    public CraftAction(@Nonnull ItemStack old, @Nonnull SlotProperty.ModuleSlot slot, @Nullable ItemModule toAdd, @Nonnull PlayerEntity player) {
        this.old = old;
        this.toAdd = toAdd;
        ItemModule.ModuleInstance instance = slot.parent;
        slotId.add(slot.id);
        if (slot.parent != null) {
            while (instance.parent != null) {
                int slotNumber = SlotProperty.getSlotNumberIn(instance);
                slotId.add(slotNumber);
                instance = instance.parent;
            }
        }
        this.player = player;
    }

    public CraftAction(PacketByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            slotId.add(buf.readInt());
        }
        String modules = buf.readString();
        if (!modules.equals("null")) {
            toAdd = Miapi.moduleRegistry.get(modules);
        } else {
            toAdd = null;
        }
        player = getPlayerFromUuid(buf.readUuid());
    }

    public PacketByteBuf toPacket(PacketByteBuf buf) {
        buf.writeInt(slotId.size());
        for (Integer slot : slotId) {
            buf.writeInt(slot);
        }
        if (toAdd != null) {
            buf.writeString(toAdd.getName());
        } else {
            buf.writeString("null");
        }
        buf.writeUuid(player.getUuid());
        return buf;
    }

    public void setItem(ItemStack stack) {
        old = stack;
    }

    public boolean canPerform() {
        ItemStack crafted = getPreview();
        AtomicBoolean test = new AtomicBoolean(true);
        forEachCraftingProperty(crafted, (guiCraftingProperty, module, inventory, start, end) -> {
            if (!test.get()) {
                test.set(guiCraftingProperty.canPerform(old, crafted, player, module, toAdd, inventory));
            }
        });
        return test.get();
    }

    public void linkInventory(Inventory inventory, int offset) {
        this.linkedInventory = inventory;
        this.inventoryOffset = offset;
    }

    public ItemStack perform() {
        final ItemStack[] craftingStack = {craft()};
        forEachCraftingProperty(craftingStack[0], (craftingProperty, module, inventory, start, end) -> {
            List<ItemStack> itemStacks = craftingProperty.performCraftAction(old, craftingStack[0], player, module, toAdd, inventory);
            craftingStack[0] = itemStacks.remove(0);
            for (int i = start; i <= end; i++) {
                inventory.set(i, itemStacks.get(i));
            }
        });
        return craftingStack[0];
    }

    private ItemStack craft() {
        ItemStack craftingStack = old.copy();
        if (!old.hasNbt() || !old.getNbt().contains("modules")) {
            Miapi.LOGGER.error("old Item has no Modules - something went very wrong");
            return old;
        }
        //remove CacheKey so new cache gets Generated
        craftingStack.getNbt().remove(ModularItemCache.cacheKey);
        ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(old);
        ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
        if (slotId.size() == 0) {
            //a module already exists, replacing module 0
            craftingStack.getNbt().putString("modules", new ItemModule.ModuleInstance(toAdd).toString());
            if (toAdd == null) {
                return ItemStack.EMPTY;
            }
            return craftingStack;
        }
        ItemModule.ModuleInstance parsingInstance = newBaseModule;
        for (int i = slotId.size() - 1; i > 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }

        if (toAdd == null) {
            parsingInstance.subModules.remove(slotId.get(0));
        } else {
            parsingInstance.subModules.put(slotId.get(0), new ItemModule.ModuleInstance(toAdd));
        }
        craftingStack.getNbt().putString("modules", newBaseModule.toString());
        //retrieve Slot from craftingStack
        //call relevant CraftingProperty functions
        return craftingStack;
    }

    public ItemStack getPreview() {
        ItemStack craftingStack = craft();
        forEachCraftingProperty(craftingStack, (guiCraftingProperty, module, inventory, start, end) -> {
            guiCraftingProperty.preview(old, craftingStack, player, module, toAdd, inventory);
        });
        return craftingStack;
    }

    public void forEachCraftingProperty(ItemStack crafted, PropertyConsumer propertyConsumer) {
        ItemModule.ModuleInstance parsingInstance = ModularItem.getModules(crafted);
        for (int i = slotId.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }

        ItemModule.ModuleInstance newInstance = parsingInstance;
        if (parsingInstance != null) {
            AtomicInteger integer = new AtomicInteger(inventoryOffset);
            newInstance.getProperties().forEach((property, json) -> {
                if (property instanceof CraftingProperty craftingProperty) {
                    List<ItemStack> itemStacks = new ArrayList<>();
                    int startPos = integer.get();
                    int endPos = startPos + craftingProperty.getSlotPositions().size();
                    for (int i = startPos; i <= endPos; i++) {
                        itemStacks.add(linkedInventory.getStack(i));
                    }
                    propertyConsumer.accept(craftingProperty, newInstance, itemStacks, startPos, endPos);
                    integer.set(endPos);
                }
            });
        }
    }

    private static PlayerEntity getPlayerFromUuid(UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerManager().getPlayer(uuid);
        } else if (MinecraftClient.getInstance() != null) {
            return MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
        }
        return null;
    }

    public interface PropertyConsumer {
        void accept(CraftingProperty craftingProperty, ItemModule.ModuleInstance moduleInstance, List<ItemStack> inventory, int start, int end);
    }
}
