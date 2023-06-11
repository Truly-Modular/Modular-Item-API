package smartin.miapi.client.gui.crafting.statdisplay;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.modules.properties.ArmorPenProperty;
import smartin.miapi.modules.properties.BlockProperty;
import smartin.miapi.modules.properties.FlexibilityProperty;
import smartin.miapi.modules.properties.HealthPercentDamage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StatDisplay extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private final BoxList boxList;
    private TransformableWidget transformableWidget;

    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_DAMAGE).setName(Text.literal("Attack Damage")).setDefault(1).build());
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_SPEED).setName(Text.literal("Attack Speed")).setDefault(4).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay.Builder(AttributeRegistry.ITEM_DURABILITY).setName(Text.literal("Durability")).setDefault(0).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(FlexibilityProperty.property).setName(Text.of("Flexibility")).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(HealthPercentDamage.property).setName(Text.of("Vitality Drain")).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(ArmorPenProperty.property).setName(Text.of("Armor Piercing")).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(BlockProperty.property).setName(Text.of("Block")).build());
    }

    public StatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        transformableWidget = new TransformableWidget(x, y, width, height, Text.empty());
        boxList = new BoxList(x, y, width, height, Text.empty(), new ArrayList<>());
        transformableWidget.addChild(boxList);
        transformableWidget.rawProjection.loadIdentity();
        transformableWidget.rawProjection.multiply(Matrix4f.scale(0.5f, 0.5f, 0.5f));
        //transformableWidget.rawProjection.multiply(Matrix4f.translate(100, 0, 0));
        //transformableWidget.rawProjection.add(Matrix4f.translate(100, 0, 0));
        addChild(transformableWidget);
        //addChild(boxList);
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
