package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;
import dev.architectury.platform.Platform;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiServerConfig {
    @AutoCodec.Name("generated_materials")
    public GeneratedMaterialsCategory generatedMaterials = new GeneratedMaterialsCategory();

    public EnchantmentsCategory enchants = new EnchantmentsCategory();

    public OtherCategory other = new OtherCategory();

    @ConfigAutoCodec.ConfigClassMarker
    public static class OtherCategory {
        @Comment("""
                Whether the development mode of Miapi is enabled
                DO NOT ENABLE IF U DONT KNOW WHAT IT DOES""")
        @AutoCodec.Name("development_mode")
        public boolean developmentMode = Platform.isDevelopmentEnvironment();

        @Comment("""
                If this is on the Block Teleports effect of Truly Modular will block most teleports,
                if false it will only block default Enderman,Chorus fruit and Ender Pearls""")

        @AutoCodec.Name("block_all_teleports_effects")
        public boolean blockAllTeleportsEffect = true;

        @Comment("Whether a server reload should be automatically forced to fix Forge having buggy class loading")
        @AutoCodec.Name("forge_reload_mode")
        public boolean forgeReloadMode = Platform.isForge();
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class GeneratedMaterialsCategory {
        @Comment("""
                Whether Miapi should automatically generate materials based on modded items
                If this is disabled, the other fields in this section will have no effect""")
        @AutoCodec.Name("generate_materials")
        public boolean generateMaterials = true;

        @Comment("The maximum amount of materials generated per type")
        @AutoCodec.Name("max_generated_materials")
        public int maximumGeneratedMaterials = 200;

        @Comment("""
                A regex used to prevent items matching the pattern from generating materials
                By default this prevents chipped and everycompat from generating materials""")
        @AutoCodec.Name("block_regex")
        public String blockRegex = "^(chipped|everycompat).*";

        @Comment("Whether Miapi should automatically generate materials based on modded wood related items")
        @AutoCodec.Name("generate_wood_materials")
        public boolean generateWoodMaterials = true;

        @Comment("Whether Miapi should automatically generate materials based on modded stone related items")
        @AutoCodec.Name("generate_stone_materials")
        public boolean generateStoneMaterials = true;

        @Comment("Whether Miapi should automatically generate materials based on modded tools")
        @AutoCodec.Name("generate_other_materials")
        public boolean generateOtherMaterials = true;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class EnchantmentsCategory {
        @Comment("Whether Modular Bows should no longer require any arrows to work infinity")
        @AutoCodec.Name("better_infinity")
        public boolean betterInfinity = true;

        @Comment("Whether loyalty should trigger in the void with Modular Items")
        @AutoCodec.Name("better_loyalty")
        public boolean betterLoyalty = true;

        @Comment("""
                Whether base modular items should have a say in valid enchantments
                For example, if this is true, a modular pickaxe will automatically be allowed pickaxe enchantments regardless of its modules""")
        @AutoCodec.Name("lenient_enchantments")
        public boolean lenientEnchantments = true;
    }
}
