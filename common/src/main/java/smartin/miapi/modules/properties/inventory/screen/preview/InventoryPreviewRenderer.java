package smartin.miapi.modules.properties.inventory.screen.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.events.ClientEvents;

import java.util.List;

public final class InventoryPreviewRenderer {
    private static final int SLOT_SIZE = 18;
    private static final int ENTRY_SPACING = 22;
    private static final int LEFT_OFFSET = 4;

    public static void setup() {
        ClientEvents.HUD_RENDER.register(InventoryPreviewRenderer::renderUI);
        ClientEvents.CLIENT_TICK.register(client -> InventoryPreviewManager.clientTick());
    }

    public static void renderUI(
            GuiGraphics graphics,
            float partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        int startY = InventoryPreviewManager.getTopOffset();

        List<InventoryPreviewManager.PreviewEntry> entries =
                InventoryPreviewManager.getActiveEntries();

        for (int entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
            InventoryPreviewManager.PreviewEntry entry =
                    entries.get(entryIndex);

            List<ItemStack> items =
                    InventoryPreviewManager.getVisibleItems(entry);

            if (items.isEmpty()) {
                continue;
            }

            int y = startY + (entryIndex * ENTRY_SPACING);

            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack stack = items.get(slot);

                int x = LEFT_OFFSET + (slot * SLOT_SIZE);

                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(
                        minecraft.font,
                        stack,
                        x,
                        y
                );
            }
        }
    }
}