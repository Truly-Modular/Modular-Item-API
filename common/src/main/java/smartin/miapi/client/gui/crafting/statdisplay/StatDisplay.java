package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.statdisplay.attributes.AttributeSingleDisplay;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StatDisplay extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private final BoxList boxList;

    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_DAMAGE).setName(Text.literal("Attack Damage")).setDefault(1).build());
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_SPEED).setName(Text.literal("Attack Speed")).setDefault(4).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay.Builder(AttributeRegistry.ITEM_DURABILITY).setName(Text.literal("Durability")).setDefault(0).build());
        addStatDisplay(new FlexibilityStatDisplay());
    }

    public StatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        boxList = new BoxList(x, y, width, height, Text.empty(), new ArrayList<>());
        addChild(boxList);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boxList.render(matrices, mouseX, mouseY, delta);
    }

    private void update() {
        List<ClickableWidget> widgets = new ArrayList<>();
        for (InteractAbleWidget statDisplay : statDisplays) {
            if (statDisplay instanceof SingleStatDisplay singleStatDisplay) {
                if (singleStatDisplay.shouldRender(original, compareTo)) {
                    statDisplay.setHeight(singleStatDisplay.getHeightDesired());
                    statDisplay.setWidth(singleStatDisplay.getWidthDesired());
                    widgets.add(statDisplay);
                }
            }
        }
        boxList.setWidgets(widgets, 1);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T statDisplay) {
        statDisplays.add(statDisplay);
    }

    public void setOriginal(@Nonnull ItemStack original) {
        this.original = original;
        update();
    }

    public void setCompareTo(@Nonnull ItemStack compareTo) {
        this.compareTo = compareTo;
        update();
    }
}
