package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;
import dev.architectury.platform.Platform;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiServerConfig {
    @AutoCodec.Name("generated_materials")
    public GeneratedMaterialsCategory generatedMaterials = new GeneratedMaterialsCategory();

    public EnchantmentsCategory enchants = new EnchantmentsCategory();

    @AutoCodec.Name("stun_effect")
    public StunEffectCategory stunEffectCategory = new StunEffectCategory();

    public OtherCategory other = new OtherCategory();

    @ConfigAutoCodec.ConfigClassMarker
    public static class OtherCategory {
        @Comment("""
                Whether the development mode of Miapi is enabled
                DO NOT ENABLE IF U DONT KNOW WHAT IT DOES""")
        @AutoCodec.Name("development_mode")
        public boolean developmentMode = Platform.isDevelopmentEnvironment();

        @Comment("""
                Truly Modular Logs more aggressivly""")
        @AutoCodec.Name("verbose_logging")
        public boolean verboseLogging = false;

        @Comment("""
                If this is on the Block Teleports effect of Truly Modular will block most teleports,
                if false it will only block default Enderman,Chorus fruit and Ender Pearls""")

        @AutoCodec.Name("block_all_teleports_effects")
        public boolean blockAllTeleportsEffect = true;

        @Comment("""
                This allows Truly Modular to dynamicly reset its Toolmaterial
                Some mods might not like dynamic Toolmaterials and cause issues with it,
                but overall it should increase compatibility and help with Tooldetection""")

        @AutoCodec.Name("loose_tool_material")
        public boolean looseToolMaterial = true;

        @Comment("""
                If this is true modular items will fully break.
                If set to false Modular Item will instead to go into a Broken state
                In this broken state they cant do anything but repaired.
                WARNING: some anvil reworking mods break the repairing logic. Please report those issues to us.
                """)
        @AutoCodec.Name("full_break_modular_items")
        public boolean fullBreakModularItems = true;

        @Comment("""
                Whether a miapi reload should be automatically forced on serverstart
                This is enabled for compat reasons, sometimes scanning recipes and other stuff during a reload isnt stable
                """)
        @AutoCodec.Name("reload_on_server_start")
        public boolean doubleReload = true;

        @Comment("""
                How much of a Modules Durability is used to repair the Item
                """)
        @AutoCodec.Name("repair_ratio")
        public double repairRatio = 1.0;
    }

    @ConfigAutoCodec.ConfigClassMarker
    public static class StunEffectCategory {
        @Comment("List of StatusEffects the player will get when stunned")
        @AutoCodec.Name("player_effects")
        public List<Identifier> playerEffects = List.of(Registries.STATUS_EFFECT.getId(StatusEffects.BLINDNESS.comp_349()), Registries.STATUS_EFFECT.getId(StatusEffects.SLOWNESS.comp_349()));

        @Comment("""
                The Stunhealth of a default entity, the StunHealth determins how much stun damage is needed to stun.
                requires restart to apply - may not correctly affect older worlds""")
        @AutoCodec.Name("stun_health")
        public double stunHealth = 20;

        @Comment("The Time a Entity is stunned")
        @AutoCodec.Name("stun_length")
        public int stunLength = 20 * 5;

        @Comment("The Time a Entity is immune to stuns after beeing stunned")
        @AutoCodec.Name("stun_resistance_length")
        public int stunResistanceLength = 20 * 30;

        @Comment("""
                Attackspeed reduction for players while beeing stunned as a Player
                requires restart to apply""")
        @AutoCodec.Name("attack_speed_factor")
        public double attackSpeedFactor = 0.5;
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
        public boolean lenientEnchantments = false;
    }
}
