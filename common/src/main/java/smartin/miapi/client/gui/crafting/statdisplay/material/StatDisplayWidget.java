package smartin.miapi.client.gui.crafting.statdisplay.material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;

public class StatDisplayWidget extends InteractAbleWidget {
    StatListWidget statListWidget;
    MaterialStatWidget materialStatWidget;

    public StatDisplayWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("miapi.statdisplay.widget"));
        materialStatWidget = null;
        statListWidget = new StatListWidget(x, y, width, height);
    }

    public void setCompareTo(ItemStack itemStack) {
        if (statListWidget != null) {
            statListWidget.setCompareTo(itemStack);
        }
        if (materialStatWidget != null) {
            materialStatWidget.setCompareTo(itemStack);
        }
    }

    public void setOriginal(ItemStack itemStack) {
        Material material = MaterialProperty.getMaterialFromIngredient(itemStack);
        if (material != null) {
            removeChild(materialStatWidget);
            materialStatWidget = new MaterialStatWidget(material, material, getX(), getY(), getWidth(), getHeight(), Component.literal("miapi.material.stat.widget"));
            removeChild(statListWidget);
            addChild(materialStatWidget);
        } else {
            removeChild(materialStatWidget);
            removeChild(statListWidget);
            addChild(statListWidget);
            statListWidget.setOriginal(itemStack);
        }
    }

    public void setItemsOriginal(ItemStack itemStack, ItemStack compareTo) {
        Material material = MaterialProperty.getMaterialFromIngredient(itemStack);
        if (material != null) {
            Material compareTO = MaterialProperty.getMaterialFromIngredient(compareTo);
            removeChild(materialStatWidget);
            materialStatWidget = new MaterialStatWidget(material,
                    compareTO == null ? material : compareTO, getX(), getY(), getWidth(), getHeight(), Component.literal("miapi.material.stat.widget"));
            removeChild(statListWidget);
            addChild(materialStatWidget);
        } else {
            removeChild(materialStatWidget);
            removeChild(statListWidget);
            addChild(statListWidget);
            statListWidget.setItemsOriginal(itemStack, compareTo);
        }
    }
}
