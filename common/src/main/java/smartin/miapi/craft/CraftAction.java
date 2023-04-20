package smartin.miapi.craft;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.AllowedSlots;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;
import smartin.miapi.network.Networking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CraftAction {
    private final ItemModule toAdd;
    private final PlayerEntity player;
    private final List<Integer> slotId = new ArrayList<>();
    private ItemStack old;
    private Inventory linkedInventory;
    private int inventoryOffset;
    public PacketByteBuf[] packetByteBuffs;

    public CraftAction(@Nonnull ItemStack old, @Nonnull SlotProperty.ModuleSlot slot, @Nullable ItemModule toAdd, @Nonnull PlayerEntity player, PacketByteBuf[] packetByteBuffs) {
        this.old = old;
        this.toAdd = toAdd;
        ItemModule.ModuleInstance instance = slot.parent;
        if (instance != null) {
            slotId.add(slot.id);
            while (instance.parent != null) {
                int slotNumber = SlotProperty.getSlotNumberIn(instance);
                slotId.add(slotNumber);
                instance = instance.parent;
            }
        }
        this.player = player;
        this.packetByteBuffs = packetByteBuffs;
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

        int numBuffers = buf.readInt();
        packetByteBuffs = new PacketByteBuf[numBuffers];
        for (int i = 0; i < numBuffers; i++) {
            packetByteBuffs[i] = new PacketByteBuf(Unpooled.buffer());
            packetByteBuffs[i].writeBytes(buf.readByteArray());
        }
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

        buf.writeInt(packetByteBuffs.length);
        for (PacketByteBuf packetByteBuf : packetByteBuffs) {
            //buf.writeBytes(packetByteBuf);
            buf.writeByteArray(packetByteBuf.array());
        }

        return buf;
    }

    public void setItem(ItemStack stack) {
        old = stack;
    }

    public boolean canPerform() {
        ItemStack crafted = getPreview();
        AtomicBoolean test = new AtomicBoolean(true);
        forEachCraftingProperty(crafted, (guiCraftingProperty, module, inventory, start, end, buffer) -> {
            if (test.get()) {
                test.set(guiCraftingProperty.canPerform(old, crafted, player, module, toAdd, inventory, buffer));
            }
        });
        return test.get();
    }

    private void updateItem(ItemStack stack, ItemModule.ModuleInstance instance) {
        while (instance.parent != null) {
            instance = instance.parent;
        }
        if (!stack.isEmpty()) {
            if (!stack.hasNbt()) {
                stack.setNbt(new NbtCompound());
            }
            stack.getNbt().putString("modules", instance.toString());
        }
    }

    public void linkInventory(Inventory inventory, int offset) {
        this.linkedInventory = inventory;
        this.inventoryOffset = offset;
    }

    public ItemStack perform() {
        final ItemStack[] craftingStack = {craft()};
        forEachCraftingProperty(craftingStack[0], (craftingProperty, module, inventory, start, end, buffer) -> {
            List<ItemStack> itemStacks = craftingProperty.performCraftAction(old, craftingStack[0], player, module, toAdd, inventory, buffer);
            updateItem(craftingStack[0], module);
            craftingStack[0] = itemStacks.remove(0);
            for (int i = start; i < end; i++) {
                linkedInventory.setStack(i, itemStacks.get(i - start));
            }
        });
        linkedInventory.markDirty();
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
        Map<Integer, ItemModule.ModuleInstance> subModuleMap = new HashMap<>();
        if (slotId.size() == 0) {
            //a module already exists, replacing module 0
            if (toAdd == null) {
                return ItemStack.EMPTY;
            }
            if (oldBaseModule != null) {
                subModuleMap = oldBaseModule.subModules;
            }
            ItemModule.ModuleInstance newModule = new ItemModule.ModuleInstance(toAdd);
            subModuleMap.forEach((id, module) -> {
                SlotProperty.ModuleSlot slot = SlotProperty.getSlots(newModule).get(id);
                if (slot != null && slot.allowedIn(module)) {
                    newModule.subModules.put(id, module);
                }
            });
            craftingStack.getNbt().putString("modules", newModule.toString());
            return craftingStack;
        }
        ItemModule.ModuleInstance parsingInstance = newBaseModule;
        for (int i = slotId.size() - 1; i > 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }

        if (toAdd == null) {
            parsingInstance.subModules.remove(slotId.get(0));
        } else {
            ItemModule.ModuleInstance newModule = new ItemModule.ModuleInstance(toAdd);
            if (parsingInstance.subModules.get(slotId.get(0)) != null) {
                subModuleMap = parsingInstance.subModules.get(slotId.get(0)).subModules;
            }
            subModuleMap.forEach((id, module) -> {
                SlotProperty.ModuleSlot slot = SlotProperty.getSlots(newModule).get(id);
                if (slot != null) {
                    Miapi.LOGGER.warn("slot is not null");
                    slot.allowed.forEach(s -> {
                        Miapi.LOGGER.warn("allowed" + s);
                    });
                    if (slot.allowedIn(module)) {
                        module.parent = newModule;
                        newModule.subModules.put(id, module);
                    }
                }
            });
            newModule.parent = parsingInstance;
            parsingInstance.subModules.put(slotId.get(0), newModule);
        }
        craftingStack.getNbt().putString("modules", newBaseModule.toString());
        return craftingStack;
    }

    public ItemStack getPreview() {
        AtomicReference<ItemStack> craftingStack = new AtomicReference<>(craft());
        forEachCraftingProperty(craftingStack.get(), (guiCraftingProperty, module, inventory, start, end, buffer) -> {
            craftingStack.set(guiCraftingProperty.preview(old, craftingStack.get(), player, module, toAdd, inventory, buffer));
        });
        return craftingStack.get();
    }

    public void setBuffer(PacketByteBuf[] buffers) {
        packetByteBuffs = buffers;
    }

    public void forEachCraftingProperty(ItemStack crafted, PropertyConsumer propertyConsumer) {
        ItemModule.ModuleInstance parsingInstance = ModularItem.getModules(crafted);
        for (int i = slotId.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }

        ItemModule.ModuleInstance newInstance = parsingInstance;
        if (parsingInstance != null) {
            AtomicInteger integer = new AtomicInteger(inventoryOffset);
            AtomicInteger counter = new AtomicInteger(0);

            List<CraftingProperty> sortedProperties =
                    newInstance.getProperties().keySet().stream()
                            .filter(property -> property instanceof CraftingProperty)
                            .map(property -> (CraftingProperty) property)
                            .sorted(Comparator.comparingDouble(CraftingProperty::getPriority))
                            .toList();
            for (CraftingProperty craftingProperty : sortedProperties) {
                List<ItemStack> itemStacks = new ArrayList<>();
                int startPos = integer.get();
                int endPos = startPos + craftingProperty.getSlotPositions().size();
                for (int i = startPos; i < endPos; i++) {
                    itemStacks.add(linkedInventory.getStack(i));
                }
                PacketByteBuf buf = Networking.createBuffer();
                if (packetByteBuffs != null && packetByteBuffs.length > counter.get()) {
                    buf = packetByteBuffs[counter.getAndAdd(1)];
                }
                propertyConsumer.accept(craftingProperty, newInstance, itemStacks, startPos, endPos, buf);
                integer.set(endPos);
            }
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
        void accept(CraftingProperty craftingProperty, ItemModule.ModuleInstance moduleInstance, List<ItemStack> inventory, int start, int end, PacketByteBuf buf);
    }
}
