package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import smartin.miapi.modules.properties.util.ComponentDescriptionable;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;

import java.util.List;

@Environment(EnvType.CLIENT)
public class FlattenedListPropertyStatDisplay<T> extends MultiComponentStatDisplay {
    public static final Text multiDirectionArrows = Text
            .literal("[").formatted(Formatting.DARK_GRAY)
            .append(Text.literal("⇅").formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    public static final Text upArrow = Text
            .literal("[").formatted(Formatting.DARK_GRAY)
            .append(Text.literal("↑").formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    public static final Text downArrow = Text
            .literal("[").formatted(Formatting.DARK_GRAY)
            .append(Text.literal("↓").formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    public static final Text noArrow = Text
            .literal("[").formatted(Formatting.DARK_GRAY)
            .append(Text.literal("-").formatted(Formatting.GRAY))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY));

    protected final DynamicCodecBasedProperty.FlattenedList<T> property;
    protected DescriptionGetter<T> componentGetter;
    public boolean placeArrowTitles = false;

    public <A extends DynamicCodecBasedProperty.FlattenedList<T> & ComponentDescriptionable<T>> FlattenedListPropertyStatDisplay(
            A property, StatDisplay.TextGetter title
    ) {
        this(property, 0, 0, 160, 32, title);
    }
    public <A extends DynamicCodecBasedProperty.FlattenedList<T> & ComponentDescriptionable<T>> FlattenedListPropertyStatDisplay(
            A property,
            int x, int y, int width, int height,
            StatDisplay.TextGetter title
    ) {
        super(x, y, width, height, title, null);
        this.hover = stack -> property.getLongDescriptionFor(property.get(stack), getScrollPosition());
        this.property = property;
        this.componentGetter = list -> property.getSimpleDescriptionFor(list, getScrollPosition()).stream().map(ComponentHolder::fromDescHolder).toList();
    }

    public FlattenedListPropertyStatDisplay<T> withLimitedDescSize(int size) {
        hoverDescription.maxWidth = size;
        return this;
    }
    public FlattenedListPropertyStatDisplay<T> withArrowsInTitle() {
        placeArrowTitles = true;
        return this;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        List<T> elements = property.get(mainStack);
        return elements != null && !elements.isEmpty();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        if (hover != null) hoverDescription.setText(hover.resolve(mainStack));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        if (hover != null) hoverDescription.setText(hover.resolve(mainStack));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public List<ComponentHolder> getComponents(ItemStack original, ItemStack compareTo) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        List<T> list = property.get(mainStack);
        maxScrollPositon = list == null ? 0 : list.size()-1;
        if (scrollPosition > maxScrollPositon) scrollPosition = maxScrollPositon;
        updateScrollIcons(mainStack);
        if (list == null || list.isEmpty()) return List.of();
        return componentGetter.resolve(list);
    }

    public void updateScrollIcons(ItemStack stack) {
        if (!placeArrowTitles) return;
        Text toAppend;
        if (maxScrollPositon < 1) toAppend = noArrow;
        else if (scrollPosition == maxScrollPositon) toAppend = upArrow;
        else if (scrollPosition == 0) toAppend = downArrow;
        else toAppend = multiDirectionArrows;
        textWidget.setText(title.resolve(stack).copy().append(" ").append(toAppend));
    }

    public interface DescriptionGetter<T> {
        List<ComponentHolder> resolve(List<T> list);
    }
}
