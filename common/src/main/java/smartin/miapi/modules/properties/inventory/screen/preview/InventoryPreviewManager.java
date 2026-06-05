package smartin.miapi.modules.properties.inventory.screen.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.ArrayList;
import java.util.List;

public final class InventoryPreviewManager {
    private static final List<PreviewEntry> ACTIVE = new ArrayList<>();
    public static final ModernNetworking.ServerToClientManager<ResourceLocation> PREVIEW = new ModernNetworking.ServerToClientManager<>(Miapi.id("inventory_use_preview"), ResourceLocation.CODEC, (id, player, access) -> {
        show(id);
    });

    private InventoryPreviewManager() {
    }

    public static int getPreviewSlots() {
        return MiapiConfig.getClientConfig().preview.count;
    }

    public static int getTopOffset() {
        return MiapiConfig.getClientConfig().preview.topDistance;
    }

    public static int getDisplayTimeTicks() {
        return 20 * MiapiConfig.getClientConfig().preview.displayTime;
    }

    public static void show(ResourceLocation inventoryTypeId) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        List<InventoryInstance> instances =
                ItemInventoryManager
                        .getAllInventories(Minecraft.getInstance().player)
                        .stream()
                        .filter(i -> i.getType().id.equals(inventoryTypeId))
                        .toList();

        if (instances.isEmpty()) {
            return;
        }

        InventoryInstance instance = instances.getFirst();
        ACTIVE.removeIf(entry ->
                entry.instance.getType().id.equals(inventoryTypeId));
        ACTIVE.add(new PreviewEntry(
                instance,
                getDisplayTimeTicks()
        ));
    }

    public static void clientTick() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if(Minecraft.getInstance().player.isUsingItem()){
            return;
        }
        ACTIVE.removeIf(entry -> --entry.remainingTicks <= 0);
    }

    public static List<PreviewEntry> getActiveEntries() {
        return ACTIVE;
    }

    public static List<ItemStack> getVisibleItems(PreviewEntry entry) {
        Container container = entry.instance.create();
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            result.add(stack);
            if (result.size() >= getPreviewSlots()) {
                break;
            }
        }
        return result;
    }

    public static final class PreviewEntry {
        public final InventoryInstance instance;
        public int remainingTicks;

        public PreviewEntry(
                InventoryInstance instance,
                int remainingTicks
        ) {
            this.instance = instance;
            this.remainingTicks = remainingTicks;
        }
    }
}