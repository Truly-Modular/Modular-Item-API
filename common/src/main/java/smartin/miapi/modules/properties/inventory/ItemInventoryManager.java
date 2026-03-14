package smartin.miapi.modules.properties.inventory;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.inventory.impl.BackpackInventoryType;
import smartin.miapi.modules.properties.inventory.impl.PlayerArmorSlotInfo;
import smartin.miapi.modules.properties.inventory.impl.QuiverInventoryType;

import java.util.HashMap;
import java.util.Map;

public class ItemInventoryManager {

    public static final Map<ResourceLocation, InventoryType> INVENTORY_TYPES = new HashMap<>();
    public static final Map<ResourceLocation, SlotInfo> PLAYER_TO_SLOT = new HashMap<>();

    static {
        registerInventoryType(new BackpackInventoryType());
        registerInventoryType(new QuiverInventoryType());

        // Armor slots
        PLAYER_TO_SLOT.put(
                Miapi.id("armor_head"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.HEAD,
                        Miapi.id("armor_head"),
                        Color.YELLOW,
                        Component.literal("Helmet")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_chest"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.CHEST,
                        Miapi.id("armor_chest"),
                        Color.GREEN,
                        Component.literal("Chestplate")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_legs"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.LEGS,
                        Miapi.id("armor_legs"),
                        Color.BLUE,
                        Component.literal("Leggings")
                )
        );

        PLAYER_TO_SLOT.put(
                Miapi.id("armor_feet"),
                new PlayerArmorSlotInfo(
                        EquipmentSlot.FEET,
                        Miapi.id("armor_feet"),
                        Color.RED,
                        Component.literal("Boots")
                )
        );
    }

    public static void registerInventoryType(InventoryType type) {
        INVENTORY_TYPES.put(type.getId(), type);
    }


    /**
     * loads container from itemstack based on InventoryType ID
     */
    public static Container loadOrCreate(ItemStack stack, ResourceLocation id) {

        Container inventory = load(stack, id);

        if (inventory != null) return inventory;

        InventoryType type = INVENTORY_TYPES.get(id);
        if (type == null) throw new IllegalArgumentException("unknowk inventory type requested!");

        return type.decode(stack, ItemContainerContents.EMPTY);
    }

    @Nullable
    private static Container load(ItemStack stack, ResourceLocation typeId) {

        InventoryType type = INVENTORY_TYPES.get(typeId);
        if (type == null) return null;

        InventoryComponent component = stack.get(InventoryComponent.ITEM_INVENTORIES);

        if (component == null) {
            return type.decode(stack, ItemContainerContents.EMPTY);
        }

        ItemContainerContents contents =
                component.inventories().getOrDefault(typeId, ItemContainerContents.EMPTY);

        return type.decode(stack, contents);
    }

    /**
     * saves Inventory back onto Itemstack after changing it
     */
    public static void save(ItemStack stack, InventoryType type, Container inventory) {

        InventoryComponent component = stack.get(InventoryComponent.ITEM_INVENTORIES);

        Map<ResourceLocation, ItemContainerContents> map =
                component != null
                        ? new HashMap<>(component.inventories())
                        : new HashMap<>();

        map.put(type.getId(), type.encode(inventory));

        stack.set(InventoryComponent.ITEM_INVENTORIES, new InventoryComponent(map));
    }
}