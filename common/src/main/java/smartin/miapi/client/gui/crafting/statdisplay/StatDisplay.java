package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.modules.properties.*;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StatDisplay extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private static final List<StatWidgetSupplier> statWidgetSupplier = new ArrayList<>();
    private final BoxList boxList;
    private TransformableWidget transformableWidget;
    private TransformableWidget hoverText;
    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        addStatDisplay(new FlattenedListPropertyStatDisplay<>(
                PotionEffectProperty.property,
                stk -> statTranslation("tipped"))
                .withLimitedDescSize(350).withArrowsInTitle());
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
        addStatDisplay(AttributeSingleDisplay
                .Builder(AttributeRegistry.BACK_STAB)
                .setTranslationKey("back_stab")
                .setDefault(1)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .Builder(AttributeRegistry.SHIELD_BREAK)
                .setTranslationKey("shield_break")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .Builder(AttributeRegistry.REACH)
                .setTranslationKey("reach")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .Builder(AttributeRegistry.ATTACK_RANGE)
                .setTranslationKey("attack_range")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(2).build());
        addStatDisplay(MiningLevelStatDisplay
                .Builder("pickaxe")
                .setAttribute(AttributeRegistry.MINING_SPEED_PICKAXE).build());
        addStatDisplay(MiningLevelStatDisplay
                .Builder("axe")
                .setAttribute(AttributeRegistry.MINING_SPEED_AXE).build());
        addStatDisplay(MiningLevelStatDisplay
                .Builder("shovel")
                .setAttribute(AttributeRegistry.MINING_SPEED_SHOVEL).build());
        addStatDisplay(MiningLevelStatDisplay
                .Builder("hoe")
                .setAttribute(AttributeRegistry.MINING_SPEED_HOE).build());
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
        ScrollList list = new ScrollList(x * 2, y * 2, width * 2, height * 2, List.of(boxList));
        transformableWidget.addChild(list);
        transformableWidget.rawProjection = new Matrix4f();
        transformableWidget.rawProjection.scale(0.5f, 0.5f, 0.5f);
        addChild(transformableWidget);
        hoverText = new TransformableWidget(x, y, width, height, Text.empty());
        hoverText.rawProjection = new Matrix4f().scale(0.667f, 0.667f, 0.667f);
        addChild(hoverText);
    }

    public static MutableText statTranslation(String statName) {
        return Text.translatable(Miapi.MOD_ID + ".stat." + statName);
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
        if (hoverDisplay != null && isMouseOver(mouseX, mouseY)) {
            float scale = 0.667f;
            hoverDisplay.setX((int) ((mouseX + 5) * (1 / scale)));
            hoverDisplay.setY((int) ((mouseY - hoverDisplay.getHeight() / 2 * scale) * (1 / scale)));
            hoverText.renderWidget(hoverDisplay, drawContext, mouseX, mouseY, delta);
        }
    }

    private <T extends InteractAbleWidget & SingleStatDisplay> void update() {
        List<ClickableWidget> widgets = new ArrayList<>();
        for (StatWidgetSupplier supplier : statWidgetSupplier) {
            List<T> statWidgets = supplier.currentList(original, compareTo);
            for (T statDisplay : statWidgets) {
                statDisplay.setHeight(statDisplay.getHeightDesired());
                statDisplay.setWidth(statDisplay.getWidthDesired());
                widgets.add(statDisplay);
            }
        }
        for (InteractAbleWidget statDisplay : statDisplays) {
            if (statDisplay instanceof SingleStatDisplay singleStatDisplay && (singleStatDisplay.shouldRender(original, compareTo))) {
                statDisplay.setHeight(singleStatDisplay.getHeightDesired());
                statDisplay.setWidth(singleStatDisplay.getWidthDesired());
                widgets.add(statDisplay);

            }
        }
        boxList.setWidgets(widgets, 1);
    }

    public interface StatWidgetSupplier {
        <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T statDisplay) {
        statDisplays.add(statDisplay);
    }

    public static void addStatDisplaySupplier(StatWidgetSupplier supplier) {
        statWidgetSupplier.add(supplier);
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

    public interface MultiTextGetter {
        List<Text> resolve(ItemStack stack);
    }
}
