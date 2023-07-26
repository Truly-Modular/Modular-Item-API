package smartin.miapi.modules.properties.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;

public interface ComponentDescriptionable<T> {
    @Environment(EnvType.CLIENT)
    List<DescriptionHolder> getSimpleDescriptionFor(List<T> elements, int scrollIndex);
    @Environment(EnvType.CLIENT)
    List<Text> getLongDescriptionFor(List<T> elements, int scrollIndex);

    /**
     * @param prefix         the prefix for this description component
     * @param scrolling      the main description text, which just so happens to scroll
     * @param scrollMaxWidth the max width before the description scrolls... set to -1 to automatically align
     */
    record DescriptionHolder(Text prefix, Text scrolling, int scrollMaxWidth) {}
}
