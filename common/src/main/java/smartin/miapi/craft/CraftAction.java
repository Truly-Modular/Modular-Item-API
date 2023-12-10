package smartin.miapi.craft;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents an action related to crafting an item with modules.
 * It contains the necessary information to perform the crafting action and to
 * update the relevant inventories or item stacks as a result of the action.
 * It also is capable of writing and reading from and to a buffer to sync itself
 * from server to client
 */
public class CraftAction {
    public final ItemModule toAdd;
    public final PlayerEntity player;
    public final List<Integer> slotId = new ArrayList<>();
    private ItemStack old;
    private final ModularWorkBenchEntity blockEntity;
    private Inventory linkedInventory;
    private int inventoryOffset;
    public Map<String, String> data = new HashMap<>();
    public static final List<CraftingEvent> events = new ArrayList<>();

    /**
     * Constructs a new instance of CraftAction, given the old item stack, the slot to modify,
     * the module to add, the player performing the action, and the packet byte buffers.
     *
     * @param old    the old item stack being modified
     * @param slot   the slot where the new module will be added
     * @param toAdd  the module to add
     * @param player the player performing the action
     * @param bench  the workbench block entity (null on client)
     * @param data   a map of additional data from Craftingproperties
     */
    public CraftAction(
            ItemStack old,
            SlotProperty.ModuleSlot slot,
            @Nullable ItemModule toAdd,
            PlayerEntity player,
            ModularWorkBenchEntity bench,
            Map<String, String> data) {
        this.old = ModularItemStackConverter.getModularVersion(old);
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
        this.blockEntity = bench;
        this.data = data;
    }

    /**
     * Constructs a new instance of CraftAction from the specified packet byte buffer.
     *
     * @param buf   the packet byte buffer from which to construct the CraftAction
     * @param bench the workbench block entity to store in this CraftAction
     */
    public CraftAction(PacketByteBuf buf, @Nullable ModularWorkBenchEntity bench) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            slotId.add(buf.readInt());
        }
        String modules = buf.readString();
        if (!modules.equals("null")) {
            toAdd = RegistryInventory.modules.get(modules);
        } else {
            toAdd = null;
        }
        player = getPlayerFromUuid(buf.readUuid());
        blockEntity = bench;

        int numBuffers = buf.readInt();
        for (int i = 0; i < numBuffers; i++) {
            String key = buf.readString();
            String value = buf.readString();
            data.put(key, value);
        }
    }

    /**
     * Converts this CraftAction instance to a packet byte buffer.
     *
     * @param buf the packet byte buffer to which to write the CraftAction
     * @return the packet byte buffer
     */
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

        buf.writeInt(data.size());
        data.forEach((key, value) -> {
            buf.writeString(key);
            buf.writeString(value);
        });

        return buf;
    }

    public void setItem(ItemStack stack) {
        old = ModularItemStackConverter.getModularVersion(stack);
    }

    /**
     * Checks if the current crafting action can be performed.
     *
     * @return true if the crafting action can be performed, false otherwise.
     */
    public boolean canPerform() {
        ItemStack crafted = getPreview();
        AtomicBoolean test = new AtomicBoolean(true);
        forEachCraftingProperty(crafted, (guiCraftingProperty, module, inventory, start, end, buffer) -> {
            if (test.get()) {
                test.set(guiCraftingProperty.canPerform(old, crafted, blockEntity, player, module, toAdd, inventory, buffer));
            }
        });
        return test.get();
    }

    /**
     * Does a full check to dynamically decide which {@link CraftingProperty}s can and can't be performed.
     *
     * @return a pair of a map pointing each {@link CraftingProperty} to whether it can be performed, and a boolean determining the overall outcome
     */
    public Pair<Map<CraftingProperty, Boolean>, Boolean> fullCanPerform() {
        Map<CraftingProperty, Boolean> map = new HashMap<>();
        ItemStack crafted = getPreview();
        AtomicBoolean test = new AtomicBoolean(true);
        forEachCraftingProperty(crafted, (guiCraftingProperty, module, inventory, start, end, dataMap) -> {
            boolean result = guiCraftingProperty.canPerform(old, crafted, blockEntity, player, module, toAdd, inventory, dataMap);
            map.put(guiCraftingProperty, result);
            if (test.get()) test.set(result);
        });
        return Pair.of(map, test.get());
    }

    /**
     * Updates the NBT data of an ItemStack with the given module instance.
     *
     * @param stack    the ItemStack to update.
     * @param instance the module instance to set.
     */
    protected void updateItem(ItemStack stack, ItemModule.ModuleInstance instance) {
        if (instance != null) {
            while (instance.parent != null) {
                instance = instance.parent;
            }
            if (!stack.isEmpty()) {
                if (!stack.hasNbt()) {
                    stack.setNbt(new NbtCompound());
                }
                instance.writeToItem(stack);
            }
        }
    }

    /**
     * Links the given inventory to this object.
     *
     * @param inventory the inventory to link.
     * @param offset    the offset to apply to the inventory slots.
     */
    public void linkInventory(Inventory inventory, int offset) {
        this.linkedInventory = inventory;
        this.inventoryOffset = offset;
    }

    /**
     * Performs the current crafting action and updates the linked inventory.
     *
     * @return the result of the crafting action.
     */
    public ItemStack perform() {
        final ItemStack[] craftingStack = {craft()};
        forEachCraftingProperty(craftingStack[0], (craftingProperty, module, inventory, start, end, buffer) -> {
            List<ItemStack> itemStacks = craftingProperty.performCraftAction(
                    old,
                    craftingStack[0],
                    player,
                    blockEntity,
                    getModifyingModuleInstance(craftingStack[0]),
                    toAdd,
                    inventory,
                    buffer);
            updateItem(craftingStack[0], module);
            craftingStack[0] = itemStacks.remove(0);
            for (int i = start; i < end; i++) {
                linkedInventory.setStack(i, itemStacks.get(i - start));
            }
        });
        ItemModule.ModuleInstance parsingInstance = ItemModule.getModules(craftingStack[0]);
        for (int i = slotId.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }
        for (CraftingEvent eventHandler : events)
            craftingStack[0] = eventHandler.onCraft(old, craftingStack[0], parsingInstance);
        linkedInventory.markDirty();
        return craftingStack[0];
    }

    /**
     * Crafts a new ItemStack based on the current state of the object.
     *
     * @return the crafted ItemStack.
     */
    private ItemStack craft() {
        ItemStack craftingStack = old.copy();

        if (!old.hasNbt() || !old.getOrCreateNbt().contains("modules")) {
            Miapi.LOGGER.error("old Item has no Modules - something went very wrong");
            return old;
        }
        //remove CacheKey so new cache gets Generated
        ItemModule.ModuleInstance oldBaseModule = ItemModule.getModules(old);
        ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
        Map<Integer, ItemModule.ModuleInstance> subModuleMap = new HashMap<>();
        if (slotId.isEmpty()) {
            //a module already exists, replacing module 0
            if (toAdd == null) {
                return ItemStack.EMPTY;
            }
            subModuleMap = oldBaseModule.subModules;
            ItemModule.ModuleInstance newModule = new ItemModule.ModuleInstance(toAdd);
            subModuleMap.forEach((id, module) -> {
                SlotProperty.ModuleSlot slot = SlotProperty.getSlots(newModule).get(id);
                if (slot != null && slot.allowedIn(module)) {
                    newModule.subModules.put(id, module);
                }
            });
            newModule.writeToItem(craftingStack);
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
                    if (slot.allowedIn(module)) {
                        module.parent = newModule;
                        newModule.subModules.put(id, module);
                    }
                }
            });
            newModule.parent = parsingInstance;
            parsingInstance.subModules.put(slotId.get(0), newModule);
        }
        newBaseModule.writeToItem(craftingStack);
        return craftingStack;
    }

    /**
     * Returns an {@link ItemStack} representing a previewStack of the item that would be crafted
     * using the current configuration of the crafting GUI.
     *
     * @return An {@link ItemStack} representing a previewStack of the item that would be crafted.
     */
    public ItemStack getPreview() {
        AtomicReference<ItemStack> craftingStack = new AtomicReference<>(craft());
        forEachCraftingProperty(craftingStack.get(), (guiCraftingProperty, module, inventory, start, end, buffer) ->
                craftingStack.set(guiCraftingProperty.preview(
                        old,
                        craftingStack.get(),
                        player,
                        blockEntity,
                        getModifyingModuleInstance(craftingStack.get()),
                        toAdd,
                        inventory,
                        buffer)));
        ItemModule.ModuleInstance parsingInstance = ItemModule.getModules(craftingStack.get());
        for (int i = slotId.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }
        for (CraftingEvent eventHandler : events) {
            craftingStack.set(eventHandler.onPreview(old, craftingStack.get(), parsingInstance));
        }
        linkedInventory.markDirty();
        return craftingStack.get();
    }

    /**
     * Sets the buffer used to send crafting information over the network.
     *
     * @param dataMap a map to send additional Data
     */
    public void setData(Map<String, String> dataMap) {
        data = dataMap;
    }

    @Nullable
    private ItemModule.ModuleInstance getModifyingModuleInstance(ItemStack itemStack){
        try{
            ItemModule.ModuleInstance parsingInstance = ItemModule.getModules(itemStack);
            for (int i = slotId.size() - 1; i >= 0; i--) {
                parsingInstance = parsingInstance.subModules.get(slotId.get(i));
            }
            return parsingInstance;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Iterates over each {@link CraftingProperty} for the crafted item and passes it to a
     * {@link PropertyConsumer} for processing.
     *
     * @param crafted          The {@link ItemStack} representing the item being crafted.
     * @param propertyConsumer The {@link PropertyConsumer} to process each {@link CraftingProperty}.
     */
    public void forEachCraftingProperty(ItemStack crafted, PropertyConsumer propertyConsumer) {
        ItemModule.ModuleInstance parsingInstance = ItemModule.getModules(crafted);
        for (int i = slotId.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.subModules.get(slotId.get(i));
        }

        AtomicInteger integer = new AtomicInteger(inventoryOffset);
        AtomicInteger counter = new AtomicInteger(0);

        ItemModule.ModuleInstance newInstance = parsingInstance;
        List<CraftingProperty> sortedProperties =
                RegistryInventory.moduleProperties.getFlatMap().values().stream()
                        .filter(CraftingProperty.class::isInstance)
                        .filter(property -> ((CraftingProperty) property).shouldExecuteOnCraft(newInstance, ItemModule.getModules(crafted), crafted))
                        .map(CraftingProperty.class::cast)
                        .sorted(Comparator.comparingDouble(CraftingProperty::getPriority))
                        .toList();
        for (CraftingProperty craftingProperty : sortedProperties) {
            List<ItemStack> itemStacks = new ArrayList<>();
            int startPos = integer.get();
            int endPos = startPos + craftingProperty.getSlotPositions().size();
            for (int i = startPos; i < endPos; i++) {
                itemStacks.add(linkedInventory.getStack(i));
            }
            propertyConsumer.accept(craftingProperty, newInstance, itemStacks, startPos, endPos, data);
            integer.set(endPos);
        }
    }

    /**
     * Returns the PlayerEntity object associated with the given UUID, if it exists.
     * If the server is running, it will use the server's PlayerManager to retrieve the player.
     * If the client is running, it will use the MinecraftClient's world to retrieve the player.
     *
     * @param uuid the UUID of the player to retrieve
     * @return the PlayerEntity object associated with the given UUID, or null if the player doesn't exist
     */
    protected static PlayerEntity getPlayerFromUuid(UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerManager().getPlayer(uuid);
        } else if (MinecraftClient.getInstance() != null) {
            assert MinecraftClient.getInstance().world != null;
            return MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
        }
        return null;
    }

    public interface PropertyConsumer {
        /**
         * A functional interface for consuming crafting properties. Used to iterate over the crafting properties
         * of an item and perform some action on each property.
         *
         * @param craftingProperty the crafting property to consume
         * @param moduleInstance   the module instance containing the crafting property
         * @param inventory        the inventory containing the items used for the crafting
         * @param start            the starting index of the crafting slots in the inventory
         * @param end              the ending index of the crafting slots in the inventory
         * @param dataMap          a Map including fields for Craftingproperty to send additional Information
         */
        void accept(CraftingProperty craftingProperty, ItemModule.ModuleInstance moduleInstance, List<ItemStack> inventory, int start, int end, Map<String, String> dataMap);
    }

    public interface CraftingEvent {
        ItemStack onCraft(ItemStack old, ItemStack crafted, @Nullable ItemModule.ModuleInstance crafting);

        ItemStack onPreview(ItemStack old, ItemStack crafted, @Nullable ItemModule.ModuleInstance crafting);
    }
}
