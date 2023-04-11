package smartin.miapi.craft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Crafter {

    public static ItemStack craft(ItemStack old, SlotProperty.ModuleSlot slot, ItemModule toAdd) {
        Miapi.LOGGER.warn(old.getNbt().toString());
        ItemStack craftingStack = old.copy();
        if (!old.hasNbt() || !old.getNbt().contains("modules")) {
            Miapi.LOGGER.error("old Item has no Modules - something went very wrong");
            return old;
        }
        //remove CacheKey so new cache gets Generated
        craftingStack.getNbt().remove(ModularItemCache.cacheKey);

        if (toAdd == null && (slot == null || slot.parent == null)) {
            //base slot remove should return air
            return ItemStack.EMPTY;
        }
        ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(old);
        ItemModule.ModuleInstance instance = slot.parent;
        ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
        Miapi.LOGGER.warn(oldBaseModule.toString());
        if (instance == null) {
            //a module already exists, replacing module 0
            craftingStack.getNbt().putString("modules", generateNew(toAdd).toString());
            return craftingStack;
        }
        List<Integer> location = new ArrayList<>();
        while (instance.parent != null) {
            int slotNumber = SlotProperty.getSlotNumberIn(instance);
            location.add(slotNumber);
            Miapi.LOGGER.error(String.valueOf(slotNumber));
            instance = instance.parent;
        }
        ItemModule.ModuleInstance parsingInstance = newBaseModule;
        for (int i = location.size() - 1; i >= 0; i--) {
            Miapi.LOGGER.warn(String.valueOf(location.get(i)));
            Miapi.LOGGER.warn(String.valueOf(parsingInstance.module.getName()));
            parsingInstance = parsingInstance.subModules.get(location.get(i));
        }
        if (toAdd == null) {
            parsingInstance.subModules.remove(slot.id);
        } else {
            parsingInstance.subModules.put(slot.id, generateNew(toAdd));
        }
        craftingStack.getNbt().putString("modules", newBaseModule.toString());
        return craftingStack;
    }

    private static ItemModule.ModuleInstance generateNew(ItemModule module){
        return null;
    }

    private static ItemModule.ModuleInstance generateNew(ItemModule module, ItemStack old, ItemStack newStack, PlayerEntity player, boolean preview) {
        ItemModule.ModuleInstance generated = new ItemModule.ModuleInstance(module);
        module.getProperties().forEach((propertyId, propertyData) -> {
            if (Miapi.modulePropertyRegistry.get(propertyId) instanceof CraftingProperty craftingProperty) {
                //Crafting Logic
            }
        });
        return generated;
    }

    public static class CraftAction {
        private final ItemModule toAdd;
        private final PlayerEntity player;
        private final List<Integer> slotId = new ArrayList<>();
        private ItemStack old;

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
            slotId.forEach(id->{
                Miapi.LOGGER.warn("Slot Id"+id);
            });
            Miapi.LOGGER.warn(slot.parent.toString());
            this.player = player;
        }

        public CraftAction(PacketByteBuf buf) {
            old = buf.readItemStack();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                slotId.add(buf.readInt());
            }
            toAdd = Miapi.moduleRegistry.get(buf.readString());
            player = getPlayerFromUuid(buf.readUuid());
        }

        public PacketByteBuf toPacket(PacketByteBuf buf) {
            buf.writeItemStack(old);
            buf.writeInt(slotId.size());
            for (Integer slot : slotId) {
                buf.writeInt(slot);
            }
            buf.writeString(toAdd.getName());
            ServerPlayerEntity entity;
            buf.writeUuid(player.getUuid());
            return buf;
        }

        public void setItem(ItemStack stack) {
            old = stack;
        }

        public boolean canPerform() {
            return true;
        }

        public ItemStack perform() {
            return getPreview();
        }

        public ItemStack getPreview() {
            ItemStack craftingStack = old.copy();
            if (!old.hasNbt() || !old.getNbt().contains("modules")) {
                Miapi.LOGGER.error("old Item has no Modules - something went very wrong");
                return old;
            }
            //remove CacheKey so new cache gets Generated
            craftingStack.getNbt().remove(ModularItemCache.cacheKey);
            ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(old);
            ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
            Miapi.LOGGER.warn(oldBaseModule.toString());
            if (slotId.size() == 0) {
                //a module already exists, replacing module 0
                craftingStack.getNbt().putString("modules", new ItemModule.ModuleInstance(toAdd).toString());
                if(toAdd==null){
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

        private static PlayerEntity getPlayerFromUuid(UUID uuid) {
            if (Miapi.server != null) {
                return Miapi.server.getPlayerManager().getPlayer(uuid);
            } else if (MinecraftClient.getInstance() != null) {
                return MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
            }
            return null;
        }
    }
}
