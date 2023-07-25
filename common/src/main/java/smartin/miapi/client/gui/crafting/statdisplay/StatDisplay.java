package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.modules.properties.ArmorPenProperty;
import smartin.miapi.modules.properties.BlockProperty;
import smartin.miapi.modules.properties.FlexibilityProperty;
import smartin.miapi.modules.properties.HealthPercentDamage;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StatDisplay extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private final BoxList boxList;
    private TransformableWidget transformableWidget;
    private TransformableWidget hoverText;

    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("1 and first"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("2 dwkad"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("3 efef"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("4 bbbb"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("5 mdallwl"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("6"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("7"), stack -> Text.literal("yep")));
        addStatDisplay(new MultiComponentStatDisplay(0, 0, 160, 32, stack -> Text.literal("8 and last"), stack -> Text.literal("yep")));
        addStatDisplay(AttributeSingleDisplay
                .Builder(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                .setTranslationKey("damage")
                .setDefault(1)
                .setMax(13.0).build());
        addStatDisplay(AttributeSingleDisplay
                .Builder(EntityAttributes.GENERIC_ATTACK_SPEED)
                .setTranslationKey("attack_speed")
                .setDefault(4)
                .setMax(4.0).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay
                .Builder(AttributeRegistry.ITEM_DURABILITY)
                .setTranslationKey("durability")
                .setDefault(0)
                .setFormat("##")
                .setMax(2000).build());
        addStatDisplay(SinglePropertyStatDisplay
                .Builder(FlexibilityProperty.property)
                .setTranslationKey(FlexibilityProperty.KEY)
                .setMax(10)
                .build());
        addStatDisplay(SinglePropertyStatDisplay
                .Builder(HealthPercentDamage.property)
                .setMax(50)
                .setTranslationKey(HealthPercentDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .Builder(ArmorPenProperty.property)
                .setMin(-20)
                .setMax(50)
                .setTranslationKey(ArmorPenProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .Builder(BlockProperty.property)
                .setMax(50)
                .setTranslationKey(BlockProperty.KEY).build());
    }

    public StatDisplay(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        transformableWidget = new TransformableWidget(x, y, width, height, Text.empty());
        boxList = new BoxList(0, 0, width * 2, height * 2, Text.empty(), new ArrayList<>());
        boxList.maxPageHeight = 100;
        ScrollList list = new ScrollList(x * 2, y * 2, width * 2, height * 2, List.of(boxList));
        transformableWidget.addChild(list);
        transformableWidget.rawProjection = new Matrix4f();
        transformableWidget.rawProjection.scale(0.5f, 0.5f, 0.5f);
        addChild(transformableWidget);
        hoverText = new TransformableWidget(x, y, width, height, Text.empty());
        hoverText.rawProjection = new Matrix4f().scale(0.667f, 0.667f, 0.667f);
        addChild(hoverText);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        Vector4f vector4f = transformableWidget.transFormMousePos(mouseX, mouseY);
        InteractAbleWidget hoverDisplay = null;
        for (Element children : boxList.children()) {
            if (children.isMouseOver(vector4f.x(), vector4f.y()) && (children instanceof SingleStatDisplay widget)) {
                InteractAbleWidget ableWidget = widget.getHoverWidget();
                if (ableWidget != null) {
                    hoverDisplay = ableWidget;
                }

            }
        }
        if (hoverDisplay != null && this.isMouseOver(mouseX, mouseY)) {
            float scale = 0.667f;
            hoverDisplay.setX((int) ((mouseX + 5) * (1 / scale)));
            hoverDisplay.setY((int) ((mouseY - hoverDisplay.getHeight() / 2 * scale) * (1 / scale)));
            hoverText.renderWidget(hoverDisplay, drawContext, mouseX, mouseY, delta);
        }
    }

    private void update() {
        List<ClickableWidget> widgets = new ArrayList<>();
        for (InteractAbleWidget statDisplay : statDisplays) {
            if (statDisplay instanceof SingleStatDisplay singleStatDisplay && (singleStatDisplay.shouldRender(original, compareTo))) {
                statDisplay.setHeight(singleStatDisplay.getHeightDesired());
                statDisplay.setWidth(singleStatDisplay.getWidthDesired());
                widgets.add(statDisplay);

            }
        }
        boxList.setWidgets(widgets, 1);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T statDisplay) {
        statDisplays.add(statDisplay);
    }

    public void setOriginal(ItemStack original) {
        this.original = original;
        update();
    }

    public void setCompareTo(ItemStack compareTo) {
        this.compareTo = compareTo;
        update();
    }

    public interface TextGetter {
        Text resolve(ItemStack stack);
    }
}
