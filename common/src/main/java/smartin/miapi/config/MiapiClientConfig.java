package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.abilities.key.MiapiBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiClientConfig {
    public static MiapiClientConfig INSTANCE = new MiapiClientConfig();

    @AutoCodec.Name("gui_colors")
    public GuiColorsCategory guiColors = new GuiColorsCategory();

    public OtherCategory other = new OtherCategory();

    @AutoCodec.Name("shielding_armor")
    public ShieldingArmorCategory shieldingArmor = new ShieldingArmorCategory();

    @AutoCodec.Name("lore")
    public LoreConfig loreConfig = new LoreConfig();

    @AutoCodec.Name("enchanting_glint")
    public EnchantingGlint enchantingGlint = new EnchantingGlint();

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

        //@AutoCodec.Name("keybinds")
        //@Comment("""
        //        Keybinds are kept in the config so they are available during gamestart.
        //        This is required as if keys are registered later the default binding is overwriten.
        //        """)
        //@AutoCodec.Ignored
        public Map<ResourceLocation, MiapiBinding> bindings = new HashMap<>();
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class LoreConfig {
        @Comment("Whether Miapi displays \"Modular Material\" when no groups are present")
        @AutoCodec.Name("inject_lore_without_material_group")
        public boolean injectLoreWithoutGroup = false;
        @Comment("Whether Miapi displays \"Modular Item\" at all")
        @AutoCodec.Name("inject_lore_modular item")
        public boolean injectLoreModularItem = true;
        @Comment("Whether Miapi displays \"Modular Material\" at all")
        @AutoCodec.Name("inject_lore_material")
        public boolean injectLoreModularMaterial = true;
        @Comment("Whether Miapi displays \"Modular Smithing Template\" at all")
        @AutoCodec.Name("inject_lore_template")
        public boolean injectLoreModularTemplate = true;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class EnchantingGlint {
        @Comment("""
                If Miapi glint is rendered at all.
                Disable this if your having issues with glint""")
        @AutoCodec.Name("enabled")
        public boolean enabled = true;

        @Comment("""
                Overwrites other glint settings, forces glint to be rendered like vanilla.
                WE RECOMMEND TO TURN THIS OFF
                """)
        @AutoCodec.Name("force_vanilla_like")
        public boolean vanillaLike = true;

        @Comment("""
                The colors Miapi uses for its default enchanting glint
                - DISABLE VANILLA LIKE!
                """)
        @AutoCodec.Name("enchanting_glint_colors")
        public List<Color> enchantColors = List.of(new Color("A755FF80"));

        @Comment("""
                Default speed of Color Change on enchanting Glint
                - DISABLE VANILLA LIKE!
                """)
        @AutoCodec.Name("enchanting_glint_speed")
        public float enchantingGlintSpeed = 1.0f;

        @Comment("""
                The Ratio of default color to Material Color.
                1.0 = only material, 0.0 is no material color.
                We recommend experiment with this setting, as it allows material colors
                to shine through much more when enchanted.
                - DISABLE VANILLA LIKE!
                """)
        @AutoCodec.Name("enchanting_material_ratio")
        public float materialRatioColor = 0.4f;
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
        @AutoCodec.Name("other_offset")
        public int otherOffsets = 0;
        @Comment("other attributes that if the player has more than 0 will offset the Armor shielding for every 20")
        @AutoCodec.Name("other_attributes")
        public List<ResourceLocation> attributesSingleLine = new ArrayList<>();
    }
}
