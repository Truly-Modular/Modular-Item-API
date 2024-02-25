package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.Comment;

import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiClientConfig {
    @AutoCodec.Name("gui_colors")
    public GuiColorsCategory guiColors = new GuiColorsCategory();

    public OtherCategory other = new OtherCategory();

    @ConfigAutoCodec.ConfigClassMarker
    public static class GuiColorsCategory {
        @Comment("The color Miapi uses for its red/invalid/negative color in the workbench gui")
        public Color red = new Color(196, 19, 19, 255);

        @Comment("The color Miapi uses for its green/valid/positive color in the workbench gui")
        public Color green = new Color(0, 255, 0, 255);

    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class OtherCategory {
        @Comment("Whether Miapi materials can be animated")
        @AutoCodec.Name("animated_materials")
        public boolean animatedMaterials = true;
        @Comment("The color Miapi uses for its enchanting glint")
        @AutoCodec.Name("enchanting_glint_colors")
        public List<Color> enchantColors = List.of(Color.MAGENTA);

        @Comment("Speed of Color Change on enchanting Glint")
        @AutoCodec.Name("enchanting_glint_speed")
        public float enchantingGlintSpeed = 1.0f;
    }
}
