package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Color;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.modules.abilities.key.MiapiBinding;

import java.util.List;
import java.util.Map;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiClientConfig {
    public static MiapiClientConfig INSTANCE = new MiapiClientConfig();

    @AutoCodec.Name("gui_colors")
    public GuiColorsCategory guiColors = new GuiColorsCategory();

    public OtherCategory other = new OtherCategory();

    public RenderCategory render = new RenderCategory();

    @AutoCodec.Name("shielding_armor")
    public ShieldingArmorCategory shieldingArmor = new ShieldingArmorCategory();

    @AutoCodec.Name("lore")
    public LoreConfig loreConfig = new LoreConfig();

    @AutoCodec.Name("enchanting_glint")
    public EnchantingGlint enchantingGlint = new EnchantingGlint();

    @AutoCodec.Name("inventory_preview")
    public InventoryUsePreview preview = new InventoryUsePreview();

    @ConfigAutoCodec.ConfigClassMarker
    public static class GuiColorsCategory {
        @Comment("The color Miapi uses for its red/invalid/negative color in the workbench gui")
        public Color red = new Color(196, 19, 19, 255);

        @Comment("The color Miapi uses for its green/valid/positive color in the workbench gui")
        public Color green = new Color(0, 255, 0, 255);

    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class InventoryUsePreview {
        @AutoCodec.Name("top_distance")
        public int topDistance = 20;

        @AutoCodec.Name("max_item_count")
        public int count = 1;

        @AutoCodec.Name("display_time")
        public int displayTime = 3;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class BatchRenderCategory {
        @AutoCodec.Name("enable_hotbar")
        public boolean enableHotBar = true;

        @AutoCodec.Name("enable_armor")
        public boolean enableArmor = true;

        @AutoCodec.Name("enable_inventory")
        public boolean enableInventory = true;
    }


    @ConfigAutoCodec.ConfigClassMarker
    public static class RenderCategory {
        @Comment("""
                batch rendering optimises modular item rendering by grouping their rendering.
                This might not work if other mods screw with the rendering internals of one of these things.
                """)
        @AutoCodec.Name("batch_rendering")
        public BatchRenderCategory batch = new BatchRenderCategory();

        @Comment("""
                requires resource pack reload to be applied (F3+T)
                might break in development mode - if stuff stops rendering set this to true
                might""")
        @AutoCodec.Name("enable_fast_render")
        public boolean enableFastRender = true;

        @Comment("""
                fast trim skips vanillas trim rendering in favor of a truly modular implementation
                in this impl. the trim is copied onto the item texture instead of rendered ontop""")
        @AutoCodec.Name("enable_fast_trim")
        public boolean enableFastTrim = true;
        @Comment("""
                enables Trail Rendering.
                Trails might be broken with certain shaders""")
        @AutoCodec.Name("enable_trail")
        public boolean enableTrailRendering = true;
        @Comment("""
                Optimised model might cause compat issues as it uses more aggressive optimisations
                if items start being invisible consider disabling this
                """)
        @AutoCodec.Name("enable_optimised_model")
        public boolean optimisedModel = true;

        @Comment("""
                The FastRenderer requires these cache sprites to be available.
                these are pre-allocated and will be used if available, but the api cannot add them on runtime.
                once these run out the slower renderer will render the overflow.
                By default its set to ~60 standard modules and 40 3D armor modules.
                consider increasing these if slowdowns with many items occur.
                """)
        @AutoCodec.Name("fast_render_cache_sprites")
        public List<CacheSprites> cacheSprites = List.of(
                new CacheSprites(16, 16, 60),
                new CacheSprites(32, 32, 10),
                new CacheSprites(64, 32, 40),
                new CacheSprites(64, 64, 10),
                new CacheSprites(128, 128, 10));

        @Comment("Whether Miapi materials can be animated")
        @AutoCodec.Name("animated_materials")
        public boolean animatedMaterials = true;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class OtherCategory {

        @Comment("If Editor is enabled despite nucleus not being loaded")
        @AutoCodec.Name("allow_editor_no_nucleus")
        public boolean allowEditorNoNucleus = false;

        @Comment("If a custom render mixin should be used to substitute nucleus editor")
        @AutoCodec.Name("enable_editor_mixin")
        public boolean enableEditorMixin = false;

        @Comment("Can split into new lines on every character, not just space")
        @AutoCodec.Name("split_new_line")
        public boolean splitNewLineAlways = false;

        @AutoCodec.Name("keybinds")
        @Comment("""
                Keybinds are kept in the config so they are available during gamestart.
                This is required so that.
                1) Rebinding on the main menu is possible
                2) Keybindings dont continously default back to their default on world load.
                                
                This isnt meant to be interacted with by users.
                """)
        //@AutoCodec.Ignored
        public Map<ResourceLocation, MiapiBinding> bindings = Map.of(
        );
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

        public boolean shouldRenderGlint() {
            return enabled && MiapiClient.CUSTOM_SHADER_LOADED;
        }

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
                Adjusting the Alpha for armor rendering
                """)
        @AutoCodec.Name("armor_alpha_adjust")
        public float armorEnchantmentAlphaAdjust = 0.8f;

        @Comment("""
                Default speed of Color Change on enchanting Glint
                - DISABLE VANILLA LIKE!
                """)
        @AutoCodec.Name("material_color_brightening")
        public float materialColorBrigtening = 0.3f;

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
    public static class CacheSprites {
        public int x;
        public int y;
        public int count;

        public CacheSprites() {

        }

        public CacheSprites(int x, int y, int count) {
            this.x = x;
            this.y = y;
            this.count = count;
        }
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class ShieldingArmorCategory {
        @Comment("the amount of other bars to be offset by")
        @AutoCodec.Name("other_offset")
        public int otherOffsets = 0;
    }
}
