package smartin.miapi.craft;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    public final Player player;
    public final List<String> slotLocation = new ArrayList<>();
    private ItemStack old;
    private final ModularWorkBenchEntity blockEntity;
    private Container linkedInventory;
    private int inventoryOffset;
    public Map<ResourceLocation, JsonElement> data = new ConcurrentHashMap<>();
    public static final List<CraftingEvent> events = new ArrayList<>();
    public CraftingScreenHandler screenHandler;

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
            Player player,
            ModularWorkBenchEntity bench,
            Map<ResourceLocation, JsonElement> data,
            CraftingScreenHandler craftingScreenHandler) {
        this.screenHandler = craftingScreenHandler;
        this.old = ModularItemStackConverter.getModularVersion(old);
        this.toAdd = toAdd;
        slotLocation.addAll(slot.getAsLocation());
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
    public CraftAction(FriendlyByteBuf buf, ModularWorkBenchEntity bench, CraftingScreenHandler craftingScreenHandler) {
        int size = buf.readInt();
        this.screenHandler = craftingScreenHandler;
        for (int i = 0; i < size; i++) {
            slotLocation.add(buf.readUtf());
        }
        String modules = buf.readUtf();
        if (!modules.equals("null")) {
            toAdd = RegistryInventory.modules.get(modules);
        } else {
            toAdd = null;
        }
        player = getPlayerFromUuid(buf.readUUID());
        blockEntity = bench;

        int numBuffers = buf.readInt();
        for (int i = 0; i < numBuffers; i++) {
            String key = buf.readUtf();
            String value = buf.readUtf();
            JsonElement element = Miapi.gson.fromJson(value, JsonElement.class);
            data.put(ResourceLocation.parse(key), element);
        }
    }

    /**
     * Converts this CraftAction instance to a packet byte buffer.
     *
     * @param buf the packet byte buffer to which to encode the CraftAction
     * @return the packet byte buffer
     */
    public FriendlyByteBuf toPacket(FriendlyByteBuf buf) {
        buf.writeInt(slotLocation.size());
        for (String slot : slotLocation) {
            buf.writeUtf(slot);
        }
        if (toAdd != null) {
            buf.writeUtf(toAdd.id().toString());
        } else {
            buf.writeUtf("null");
        }
        buf.writeUUID(player.getUUID());

        buf.writeInt(data.size());
        data.forEach((key, value) -> {
            buf.writeUtf(key.toString());
            String jsonString = Miapi.gson.toJson(value);
            buf.writeUtf(jsonString);
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
                test.set(guiCraftingProperty.canPerform(old, crafted, blockEntity, player, this, toAdd, inventory, buffer));
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
            boolean result = guiCraftingProperty.canPerform(old, crafted, blockEntity, player, this, toAdd, inventory, dataMap);
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
    protected void updateItem(ItemStack stack, ModuleInstance instance) {
        instance.getRoot().writeToItem(stack);
    }

    /**
     * Links the given inventory to this object.
     *
     * @param inventory the inventory to link.
     * @param offset    the offset to apply to the inventory slots.
     */
    public void linkInventory(Container inventory, int offset) {
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
        ComponentApplyProperty.updateItemStack(craftingStack[0], player.level().registryAccess());
        forEachCraftingProperty(craftingStack[0], (craftingProperty, module, inventory, start, end, buffer) -> {
            List<ItemStack> itemStacks = craftingProperty.performCraftAction(
                    old,
                    craftingStack[0],
                    player,
                    blockEntity,
                    this,
                    toAdd,
                    inventory,
                    buffer);
            craftingStack[0] = itemStacks.removeFirst();
            if (module != null) {
                //updateItem(craftingStack[0], module);
            }
            for (int i = start; i < end; i++) {
                linkedInventory.setItem(i, itemStacks.get(i - start));
            }
        });
        ComponentApplyProperty.updateItemStack(craftingStack[0], player.level().registryAccess());
        ModuleInstance parsingInstance = ItemModule.getModules(craftingStack[0]);
        for (int i = slotLocation.size() - 1; i >= 0; i--) {
            parsingInstance = parsingInstance.getSubModuleMap().get(slotLocation.get(i));
        }
        for (CraftingEvent eventHandler : events)
            craftingStack[0] = eventHandler.onCraft(old, craftingStack[0], parsingInstance);
        linkedInventory.setChanged();
        return craftingStack[0];
    }

    /**
     * Crafts a new ItemStack based on the current state of the object.
     *
     * @return the crafted ItemStack.
     */
    private ItemStack craft() {
        ItemStack craftingStack = old.copy();

        //remove CacheKey so new cache gets Generated
        ModuleInstance oldBaseModule = ItemModule.getModules(old);
        ModuleInstance newBaseModule = oldBaseModule.copy();
        Map<String, ModuleInstance> subModuleMap = new HashMap<>();
        if (slotLocation.isEmpty()) {
            //a module already exists, replacing module 0
            if (toAdd == null || toAdd == ItemModule.empty || toAdd.id().equals(Miapi.id("empty"))) {
                return ItemStack.EMPTY;
            }
            subModuleMap = oldBaseModule.getSubModuleMap();
            ModuleInstance newModule = new ModuleInstance(toAdd);
            subModuleMap.forEach((id, module) -> {
                SlotProperty.ModuleSlot slot = SlotProperty.getSlots(newModule).get(id);
                if (slot != null && slot.allowedIn(module)) {
                    newModule.setSubModule(id, module);
                    newModule.sortSubModule();
                }
            });
            newModule.writeToItem(craftingStack);
            newModule.clearCaches();
            return craftingStack;
        }
        ModuleInstance parsingInstance = newBaseModule;
        for (int i = slotLocation.size() - 1; i > 0; i--) {
            if (parsingInstance != null) {
                parsingInstance = parsingInstance.getSubModule(slotLocation.get(i));
            } else {
                Miapi.LOGGER.error("Critical error in replace Logic!, step craft slot position of crafting was not found!");
                slotLocation.forEach(slot -> {
                    Miapi.LOGGER.error("slot id" + slot);
                });
                return old;
            }
        }

        if ((toAdd == null || toAdd == ItemModule.empty || toAdd.id().equals(Miapi.id("empty"))) && parsingInstance != null) {
            parsingInstance.removeSubModule(slotLocation.getFirst());
        } else {
            ModuleInstance newModule = new ModuleInstance(toAdd);
            ModuleInstance moduleMapInstance = parsingInstance.getSubModule(slotLocation.getFirst());
            if (moduleMapInstance != null) {
                subModuleMap = moduleMapInstance.getSubModuleMap();
            }
            subModuleMap.forEach((id, module) -> {
                SlotProperty.ModuleSlot slot = SlotProperty.getSlots(newModule).get(id);
                if (slot != null) {
                    if (slot.allowedIn(module)) {
                        newModule.setSubModule(id, module);
                    }
                }
            });
            parsingInstance.setSubModule(slotLocation.getFirst(), newModule);
        }
        newBaseModule.writeToItem(craftingStack);
        craftingStack = craftingStack.copy();
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
                        this,
                        toAdd,
                        inventory,
                        buffer)));
        ComponentApplyProperty.updateItemStack(craftingStack.get(), player.level().registryAccess());
        ModuleInstance parsingInstance = ItemModule.getModules(craftingStack.get());
        for (int i = slotLocation.size() - 1; i >= 0; i--) {
            if (parsingInstance == null) {
                Miapi.LOGGER.error("Critical error in replace Logic!, step preview slot position of crafting was not found!");
                slotLocation.forEach(slot -> {
                    Miapi.LOGGER.error("slot id" + slot);
                });
                return old;
            }
            parsingInstance = parsingInstance.getSubModule(slotLocation.get(i));
        }
        for (CraftingEvent eventHandler : events) {
            craftingStack.set(eventHandler.onPreview(old, craftingStack.get(), parsingInstance));
        }
        linkedInventory.setChanged();
        return craftingStack.get();
    }

    /**
     * Sets the buffer used to send crafting information over the network.
     *
     * @param dataMap a map to send additional Data
     */
    public void setData(Map<ResourceLocation, JsonElement> dataMap) {
        data = dataMap;
    }

    @Nullable
    public ModuleInstance getModifyingModuleInstance(ItemStack itemStack) {
        ModuleInstance parsingInstance = ItemModule.getModules(itemStack);
        for (int i = slotLocation.size() - 1; i >= 0; i--) {
            if (parsingInstance == null) {
                Miapi.LOGGER.error("Critical error in replace Logic!, step modifiying module slot position of crafting was not found!");
                slotLocation.forEach(slot -> {
                    Miapi.LOGGER.error("slot id" + slot);
                });
                return parsingInstance;
            }
            parsingInstance = parsingInstance.getSubModule(slotLocation.get(i));
        }
        return parsingInstance;
    }

    /**
     * Iterates over each {@link CraftingProperty} for the crafted item and passes it to a
     * {@link PropertyConsumer} for processing.
     *
     * @param crafted          The {@link ItemStack} representing the item being crafted.
     * @param propertyConsumer The {@link PropertyConsumer} to process each {@link CraftingProperty}.
     */
    public void forEachCraftingProperty(ItemStack crafted, PropertyConsumer propertyConsumer) {
        ModuleInstance parsingInstance = ItemModule.getModules(crafted);
        for (int i = slotLocation.size() - 1; i >= 0; i--) {
            if (parsingInstance == null) {
                Miapi.LOGGER.error("Critical error in replace Logic!, step crafting property slot position of crafting was not found!");
                slotLocation.forEach(slot -> {
                    Miapi.LOGGER.error("slot id" + slot);
                });
                return;
            }
            parsingInstance = parsingInstance.getSubModule(slotLocation.get(i));
        }

        AtomicInteger integer = new AtomicInteger(inventoryOffset);

        ModuleInstance newInstance = parsingInstance;
        List<CraftingProperty> sortedProperties =
                RegistryInventory.moduleProperties.getFlatMap().values().stream()
                        .filter(CraftingProperty.class::isInstance)
                        .filter(property -> ((CraftingProperty) property).shouldExecuteOnCraft(newInstance, ItemModule.getModules(crafted), crafted, this))
                        .map(CraftingProperty.class::cast)
                        .sorted(Comparator.comparingDouble(CraftingProperty::getPriority))
                        .toList();
        for (CraftingProperty craftingProperty : sortedProperties) {
            List<ItemStack> itemStacks = new ArrayList<>();
            int startPos = integer.get();
            int endPos = startPos + craftingProperty.getSlotPositions().size();
            for (int i = startPos; i < endPos; i++) {
                itemStacks.add(linkedInventory.getItem(i));
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
    protected static Player getPlayerFromUuid(UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerList().getPlayer(uuid);
        } else if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getPlayerByUUID(uuid);
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
        void accept(CraftingProperty craftingProperty, ModuleInstance moduleInstance, List<ItemStack> inventory, int start, int end, Map<ResourceLocation, JsonElement> dataMap);
    }

    public interface CraftingEvent {
        ItemStack onCraft(ItemStack old, ItemStack crafted, @Nullable ModuleInstance crafting);

        ItemStack onPreview(ItemStack old, ItemStack crafted, @Nullable ModuleInstance crafting);
    }
}
