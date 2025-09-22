package smartin.miapi.modules.edit_options.material;

import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;

import java.util.*;

public class MaterialViewerWidget extends InteractAbleWidget {
    ScrollList list;
    MaterialDisplayWidget materialDisplayWidget;


    public MaterialViewerWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("material-lexica"));
        Map<String, Set<Material>> materialMap = new HashMap<>();
        MaterialProperty.MATERIAL_REGISTRY.getFlatMap().values().forEach(m -> {
            m.getGuiGroups().forEach(s -> {
                if (!m.getStringID().equals(s)) {
                    materialMap.computeIfAbsent(s, (a) -> new HashSet<>()).add(m);
                }
            });
        });
        list = new ScrollList(x, y, width, height,
                materialMap.keySet().stream().map(key -> (InteractAbleWidget) new MaterialGroupWidget(x, y, width, key, materialMap.get(key),this::setMaterial)).toList());
        addChild(list);
    }

    public void setMaterial(Material material) {
        this.removeChild(list);
        materialDisplayWidget = new MaterialDisplayWidget(this.getX(), this.getY(), this.width, this.height, material, this::clearMaterial);
        this.addChild(materialDisplayWidget);
    }

    public void clearMaterial() {
        this.removeChild(materialDisplayWidget);
        this.addChild(list);
    }
}
