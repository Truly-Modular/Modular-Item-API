package smartin.miapi.modules.properties.util;

import net.minecraft.text.Text;

import java.util.List;

public interface ComponentDescriptionable<T> {
    List<DescriptionHolder> getSimpleDescriptionFor(List<T> elements, int scrollIndex);
    List<Text> getLongDescriptionFor(List<T> elements, int scrollIndex);

    /**
     * @param prefix         the prefix for this description component
     * @param scrolling      the main description text, which just so happens to scroll
     * @param scrollMaxWidth the max width before the description scrolls... set to -1 to automatically align
     * @param scrollingColor color of the scrolling text because smartin decided to make his own special system that doesn't support text component color
     */
    record DescriptionHolder(Text prefix, Text scrolling, int scrollMaxWidth, int scrollingColor) {}
}
