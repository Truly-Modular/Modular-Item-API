package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiClientConfig {
    @AutoCodec.Name("gui_colors")
    public GuiColorsCategory guiColors = new GuiColorsCategory();

    public OtherCategory other = new OtherCategory();

    @AutoCodec.Name("shielding_armor")
    public ShieldingArmorCategory shieldingArmor = new ShieldingArmorCategory();

    @ConfigAutoCodec.ConfigClassMarker
    public static class GuiColorsCategory {
        @Comment("The color Miapi uses for its red/invalid/negative color in the workbench gui")
        public Color red = new Color(196, 19, 19, 255);

        @Comment("The color Miapi uses for its green/valid/positive color in the workbench gui")
        public Color green = new Color(0, 255, 0, 255);

    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class OtherCategory {
        @Comment("If items are rescaled in the UI, if disabled makes item overflow into other slots")
        @AutoCodec.Name("ui_scale")
        public boolean uiScale = true;
        @Comment("If Glint should render on Modular Items at all")
        @AutoCodec.Name("enchanting_glint")
        public boolean enchantingGlint = true;
        @Comment("Whether Miapi materials can be animated")
        @AutoCodec.Name("animated_materials")
        public boolean animatedMaterials = true;
        @Comment("Whether Miapi displays \"Modular Material\" when no groups are present")
        @AutoCodec.Name("inject_lore_without_material_group")
        public boolean injectLoreWithoutGroup = false;
        @Comment("Whether Miapi displays \"Modular Item\" at all")
        @AutoCodec.Name("inject_lore_modular item")
        public boolean injectLoreModularItem = true;
        @Comment("Whether Miapi displays \"Modular Material\" at all")
        @AutoCodec.Name("inject_lore_material")
        public boolean injectLoreModularMaterial = true;
        @Comment("The color Miapi uses for its enchanting glint")
        @AutoCodec.Name("enchanting_glint_colors")
        public List<Color> enchantColors = List.of(Color.MAGENTA);

        @Comment("Speed of Color Change on enchanting Glint")
        @AutoCodec.Name("enchanting_glint_speed")
        public float enchantingGlintSpeed = 1.0f;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class ShieldingArmorCategory {
        @Comment("If the Health bar is used to offset the Armor Shielding Bar")
        @AutoCodec.Name("respect_health")
        public boolean respectHealth = true;
        @Comment("If the Health bar is used to offset the Armor Shielding Bar")
        @AutoCodec.Name("respect_armor")
        public boolean respectArmor = true;
        @Comment("the amount of other bars to be offset by")
        @AutoCodec.Name("other_offest")
        public int otherOffests = 0;
        @Comment("other attributes that if the player has more than 0 will offset the Armor shielding for every 20")
        @AutoCodec.Name("other_attributes")
        public List<Identifier> attributesSingleLine = new ArrayList<>();
    }
}
