package smartin.miapi.modules.properties.inventory;

import com.redpxnda.nucleus.util.Color;
import dev.architectury.event.EventResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.events.MiapiEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemInventoryManager {
    public static final Map<ResourceLocation, Function<Player, SlotInfo>> PLAYER_TO_SLOT = new HashMap<>();
    public static final Map<ResourceLocation, Function<ItemStack, InventoryType>> CONTAINERS_FOR_ITEMSTACK = new HashMap<>();

    public static KeyMapping open = MiapiClient.KEY_BINDINGS.register(Miapi.id("open_backpack"), new KeyMapping("miapi.binding.open_backpack", 0, "miapi.binding"));

    public static void setup() {
        // Register armor slots
        registerArmorSlots();

        // Register backpack inventory type
        CONTAINERS_FOR_ITEMSTACK.put(Miapi.id("backpack"), stack -> {
            var size = InventoryProperty.property.getValue(stack);
            if (size.isPresent()) {
                return new BackpackInventory(stack, size.get());
            }
            return null;
        });

        MiapiEvents.PLAYER_TICK_END.register(new MiapiEvents.PlayerTickEvent() {
            @Override
            public EventResult tick(Player player) {
                if (open.consumeClick() && player.level().isClientSide) {
                    /*
                    Minecraft.getInstance().setScreen(new CustomInventoryMenu(
                            0,
                            player.getInventory(),
                            Component.translatable("miapi.inventory.title")
                    ));

                     */
                }
                return EventResult.pass();
            }
        });
    }

    private static void registerArmorSlots() {
        // Register each armor slot with a random color
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                final Color color = new Color(Color.AQUA);
                PLAYER_TO_SLOT.put(Miapi.id("armor_" + slot.getName()), player -> new ArmorSlotInfo(player, slot, color));
            }
        }
    }

    public static Map<SlotInfo, List<InventoryType>> getInventoryForPlayer(Player player) {
        Map<SlotInfo, List<InventoryType>> map = new HashMap<>();
        PLAYER_TO_SLOT.forEach((id, function) -> {
            SlotInfo info = function.apply(player);
            if (!info.getStack().isEmpty()) {
                List<InventoryType> inventoryTypes = new ArrayList<>();
                CONTAINERS_FOR_ITEMSTACK.forEach((slotId, inventoryTypeFunction) -> {
                    InventoryType type = inventoryTypeFunction.apply(info.getStack());
                    if (type != null && type.getSize() > 0) {
                        inventoryTypes.add(type);
                    }
                });
                if (!inventoryTypes.isEmpty()) {
                    map.put(info, inventoryTypes);
                }
            }
        });
        return map;
    }

    /**
     * This interface allows different Inventory sources.
     * f.e. when call {@link  ItemInventoryManager#PLAYER_TO_SLOT} is called this should resolve to items like the different armor slots.
     * Any Slot that may provide a partInventory needs to be implemented with this and registered at{@link  ItemInventoryManager#PLAYER_TO_SLOT}
     */
    public interface SlotInfo {
        ItemStack getStack();

        Color getColor();

        Component getName();

        void renderIcon(int x, int y, int width, int height);

        void setStack(ItemStack saved);
    }

    /**
     * With this interface different Inventory types may be implemented.
     * I requires a function to be registered {@link  ItemInventoryManager#CONTAINERS_FOR_ITEMSTACK}
     * that resolves all items from a player that should contain an inventory {@link  ItemInventoryManager#PLAYER_TO_SLOT} via this
     * and gives its Inventory content for the registered Resource location.
     * It may block certain Item Types or implement custom methods for calculating its total size.
     */
    public interface InventoryType {
        ItemContainerContents getContents();

        boolean canEnter(ItemStack itemStack);

        int getSize();

        void saveToItem();

        Component getName();
    }

    public static class ArmorSlotInfo implements SlotInfo {
        private final Player player;
        private final EquipmentSlot slot;
        private final Color color;

        public ArmorSlotInfo(Player player, EquipmentSlot slot, Color color) {
            this.player = player;
            this.slot = slot;
            this.color = color;
        }

        @Override
        public ItemStack getStack() {
            return player.getItemBySlot(slot);
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public Component getName() {
            return Component.translatable("miapi.slot.armor." + slot.getName());
        }

        @Override
        public void renderIcon(int x, int y, int width, int height) {
            // TODO: Implement icon rendering for armor slots
        }

        @Override
        public void setStack(ItemStack stack) {
            player.setItemSlot(slot, stack);
        }
    }

    public static class BackpackInventory implements InventoryType {
        private final ItemStack stack;
        private final int size;

        public BackpackInventory(ItemStack stack, double size) {
            this.stack = stack;
            this.size = (int) size;
        }

        @Override
        public ItemContainerContents getContents() {
            // TODO: Implement getting contents from the item's NBT
            return ItemContainerContents.fromItems(List.of());
        }

        @Override
        public boolean canEnter(ItemStack itemStack) {
            return true; // Allow any item for now
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public void saveToItem() {
            // TODO: Implement saving contents to the item's NBT
        }

        @Override
        public Component getName() {
            return Component.translatable("miapi.inventory.backpack");
        }
    }
}
