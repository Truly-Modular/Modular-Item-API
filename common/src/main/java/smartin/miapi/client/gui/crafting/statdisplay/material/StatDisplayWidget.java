package smartin.miapi.client.gui.crafting.statdisplay.material;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

public class StatDisplayWidget extends InteractAbleWidget {
    StatListWidget statListWidget;
    MaterialStatWidget materialStatWidget;
    Material lastMaterial = null;

    public StatDisplayWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal("miapi.statdisplay.widget"));
        materialStatWidget = null;
        statListWidget = new StatListWidget(x, y, width, height);
    }

    public void setCompareTo(ItemStack itemStack) {
        statListWidget.setCompareTo(itemStack);
    }

    public void setOriginal(ItemStack itemStack) {
        Material material = MaterialProperty.getMaterialFromIngredient(itemStack);
        if (material != null && !(itemStack.getItem() instanceof ModularItem)) {
            removeChild(materialStatWidget);
            removeChild(statListWidget);
            if (material != lastMaterial) {
                materialStatWidget = new MaterialStatWidget(material, getX(), getY(), getWidth(), getHeight(), Text.literal("miapi.material.stat.widget"));
                lastMaterial = material;
            }
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
        if (material != null && !(itemStack.getItem() instanceof ModularItem)) {
            removeChild(materialStatWidget);
            removeChild(statListWidget);
            materialStatWidget = new MaterialStatWidget(material, getX(), getY(), getWidth(), getHeight(), Text.literal("miapi.material.stat.widget"));
            addChild(materialStatWidget);
        } else {
            removeChild(materialStatWidget);
            removeChild(statListWidget);
            addChild(statListWidget);
            statListWidget.setItemsOriginal(itemStack, compareTo);
        }
    }
}
