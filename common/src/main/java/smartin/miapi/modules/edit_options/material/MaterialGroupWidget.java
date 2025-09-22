package smartin.miapi.modules.edit_options.material;

import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.crafter.replace.hover.HoverMaterialList;
import smartin.miapi.material.base.Material;

import java.util.Set;
import java.util.function.Consumer;

public class MaterialGroupWidget extends InteractAbleWidget {
    private final Set<Material> materials;
    private final SimpleButton<Void> headerButton;
    private boolean isOpen = false;
    private final Consumer<Material> setMaterial;

    public MaterialGroupWidget(int x, int y, int width, String groupName, Set<Material> materials, Consumer<Material> setMaterial) {
        super(x, y, width, 14, HoverMaterialList.getTranslation(groupName));
        this.materials = materials;
        this.setMaterial = setMaterial;

        // Group header button
        headerButton = new SimpleButton<>(
                x, y, width, 14,
                HoverMaterialList.getTranslation(groupName),
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
                            setMaterial.accept(m);
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
