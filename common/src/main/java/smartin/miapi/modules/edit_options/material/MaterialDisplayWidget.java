package smartin.miapi.modules.edit_options.material;

import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.MaterialDetailView;
import smartin.miapi.material.base.Material;

public class MaterialDisplayWidget extends InteractAbleWidget {


    public MaterialDisplayWidget(int x, int y, int width, int height, Material material, Runnable back) {
        super(x, y, width, height, Component.literal("material-widget"));
        MaterialDetailView materialDetailView = new MaterialDetailView(getX(), getY(), getWidth(), getHeight(), material, (s) -> {
            back.run();
        });
        this.addChild(materialDetailView);
    }
}
