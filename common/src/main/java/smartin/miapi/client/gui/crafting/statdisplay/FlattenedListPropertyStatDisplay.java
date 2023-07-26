package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.properties.util.ComponentDescriptionable;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;

import java.util.List;

@Environment(EnvType.CLIENT)
public class FlattenedListPropertyStatDisplay<T> extends MultiComponentStatDisplay {
    protected final DynamicCodecBasedProperty.FlattenedList<T> property;
    protected DescriptionGetter<T> componentGetter;

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

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original, compareTo);
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        List<T> elements = property.get(mainStack);
        return elements != null && !elements.isEmpty();
    }

    @Override
    public List<ComponentHolder> getComponents(ItemStack original, ItemStack compareTo) {
        ItemStack mainStack = compareTo.isEmpty() ? original : compareTo;
        List<T> list = property.get(mainStack);
        maxScrollPositon = list == null ? 0 : list.size()-1;
        if (list == null || list.isEmpty()) return List.of();
        return componentGetter.resolve(list);
    }

    public interface DescriptionGetter<T> {
        List<ComponentHolder> resolve(List<T> list);
    }
}
