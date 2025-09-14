package smartin.miapi.modules.edit_options.material;

import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.material.base.Material;

import java.util.Set;

public class MaterialGroupWidget extends InteractAbleWidget {
    private final Set<Material> materials;
    private final SimpleButton<Void> headerButton;
    private boolean isOpen = false;

    public MaterialGroupWidget(int x, int y, int width, String groupName, Set<Material> materials) {
        super(x, y, width, 14, Component.literal(groupName));
        this.materials = materials;

        // Group header button
        headerButton = new SimpleButton<>(
                x, y, width, 14,
                Component.literal(groupName),
                this::toggleOpen
        );
        headerButton.textWidget.setOrientation(ScrollingTextWidget.Orientation.LEFT);
        this.addChild(headerButton);

        this.height = 14;
    }

    private void toggleOpen() {
        if (isOpen) {
            // Close: remove material buttons
            this.children().removeIf(w -> w != headerButton);
            this.height = 14;
        } else {
            // Open: add material buttons
            int yOffset = getY() + 14;
            for (Material m : materials) {
                SimpleButton<Void> matButton = new SimpleButton<>(
                        getX() + 4, yOffset, width - 4, 14,
                        m.getTranslation(),
                        () -> {

                        }
                );
                this.addChild(matButton);
                yOffset += 14;
            }
            this.height = yOffset - getY();
        }
        isOpen = !isOpen;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        headerButton.setY(y);

        if (isOpen) {
            int yOffset = y + 14;
            for (var w : this.children()) {
                if (w != headerButton) {
                    if (w instanceof InteractAbleWidget i) {
                        i.setY(yOffset);
                    }
                    yOffset += 14;
                }
            }
            this.height = yOffset - y;
        }
    }
}
