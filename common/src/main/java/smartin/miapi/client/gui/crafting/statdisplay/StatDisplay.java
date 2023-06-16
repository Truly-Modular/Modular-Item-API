package smartin.miapi.client.gui.crafting.statdisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import smartin.miapi.Miapi;
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
    private TransformableWidget hoverText;

    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_DAMAGE).setTranslationKey("damage").setDefault(1).build());
        addStatDisplay(AttributeSingleDisplay.Builder(EntityAttributes.GENERIC_ATTACK_SPEED).setTranslationKey("attack_speed").setDefault(4).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay.Builder(AttributeRegistry.ITEM_DURABILITY).setTranslationKey("durability").setDefault(0).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(FlexibilityProperty.property).setTranslationKey(FlexibilityProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(HealthPercentDamage.property).setTranslationKey(HealthPercentDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(ArmorPenProperty.property).setTranslationKey(ArmorPenProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay.Builder(BlockProperty.property).setTranslationKey(BlockProperty.KEY).build());
    }

    public StatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        transformableWidget = new TransformableWidget(x, y, width, height, Text.empty());
        boxList = new BoxList(x * 2, y * 2, width * 2, height * 2, Text.empty(), new ArrayList<>());
        transformableWidget.addChild(boxList);
        transformableWidget.rawProjection.loadIdentity();
        transformableWidget.rawProjection.multiply(Matrix4f.scale(0.5f, 0.5f, 0.5f));
        addChild(transformableWidget);
        hoverText = new TransformableWidget(x, y, width, height, Text.empty());
        hoverText.rawProjection = Matrix4f.scale(0.667f, 0.667f, 0.667f);
        addChild(hoverText);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        Vector4f vector4f = transformableWidget.transFormMousePos(mouseX, mouseY);
        InteractAbleWidget hoverDisplay = null;
        for (Element children : boxList.children()) {
            if (children.isMouseOver(vector4f.getX(), vector4f.getY())) {
                if (children instanceof SingleStatDisplay widget) {
                    InteractAbleWidget ableWidget = widget.getHoverWidget();
                    if (ableWidget != null) {
                        hoverDisplay = ableWidget;
                    }
                }
            }
        }
        if (hoverDisplay != null) {
            float scale = 0.667f;
            hoverDisplay.x = (int) ((mouseX + 5) * (1 / scale));
            hoverDisplay.y = (int) ((mouseY - hoverDisplay.getHeight() / 2 * scale) * (1 / scale));

            hoverText.renderWidget(hoverDisplay, matrices, mouseX, mouseY, delta);

            //hoverDisplay.render(matrices, mouseX, mouseY, delta);
        }
        //drawSquareBorder(matrices, x, y, width, height, 1, ColorHelper.Argb.getArgb(255, 255, 0, 255));
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
