package smartin.miapi.config;

import dev.architectury.platform.Platform;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.config.oro_config.*;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.cache.ModularItemCache;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;

public class MiapiConfig extends Config {
    public static ServerConfig serverConfig = new ServerConfig();
    public static ClientConfig clientConfig = new ClientConfig();
    protected static MiapiConfig INSTANCE = new MiapiConfig();

    public static MiapiConfig getInstance() {
        return INSTANCE;
    }

    protected MiapiConfig() {
        super(List.of(clientConfig, serverConfig),
                new File(Platform.getConfigFolder().toString(), "miapi.json"),
                "miapi_server");
        if (Platform.isModLoaded("cloth_config")) {
        }
        this.readConfigFromFile();
        this.saveConfigToFile();
        ReloadEvents.START.subscribe((isClient -> {
            readConfigFromFile();
        }));
    }

    public static class ClientConfig extends ConfigItemGroup {
        public static ColorGroup colorConfig = new ColorGroup();
        public static CompatGroup compatGroup = new CompatGroup();

        protected ClientConfig() {
            super(of(compatGroup, colorConfig), "client");
        }
    }

    public static class ColorGroup extends ConfigItemGroup {
        public static IntegerConfigItem red = new IntegerConfigItem(
                "red",
                ColorHelper.Argb.getArgb(255, 196, 19, 19),
                "red for gui");
        public static IntegerConfigItem green = new IntegerConfigItem(
                "green",
                ColorHelper.Argb.getArgb(255, 0, 255, 0),
                "green for gui");

        protected ColorGroup() {
            super(of(red, green), "Gui Colors");
        }
    }

    public static class CompatGroup extends ConfigItemGroup {
       public static BooleanConfigItem animatedMaterial = new BooleanConfigItem(
                "animated_materials",
                true,
                "Animated Materials do have a some fps impact at the moment");
        public static BooleanConfigItem generateMaterial = new BooleanConfigItem(
                "generate_materials",
                true,
                "Generate Materials in general");
        public static StringConfigItem blockRegexGeneratedMaterials = new StringConfigItem(
                "block_regex",
                "^(chipped|everycompat).*",
                "regex to block generation"
        );
        public static BooleanConfigItem generateOtherMaterials = new BooleanConfigItem(
                "other_materials",
                true,
                "Generate Materials for Tools");
        public static BooleanConfigItem generateWoodMaterials = new BooleanConfigItem(
                "wood_materials",
                true,
                "Generate Wood related Materials");
        public static BooleanConfigItem generateStoneMaterials = new BooleanConfigItem(
                "stone_materials",
                true,
                "Generate Stone related Materials");


        protected CompatGroup() {
            super(of(animatedMaterial), "compat_settings");
            animatedMaterial.changeListener.add((renderModeConfigItem -> {
                ModularItemCache.discardCache();
            }));
        }
    }

    public static class ServerConfig extends ConfigItemGroup {
        public static EnchantmentGroup enchantmentGroup = new EnchantmentGroup();
        public static OtherConfigGroup otherGroup = new OtherConfigGroup();

        protected ServerConfig() {
            super(of(otherGroup, enchantmentGroup), "server");
        }
    }

    public static class OtherConfigGroup extends ConfigItemGroup {
        public static BooleanConfigItem developmentMode = new BooleanConfigItem(
                "development_mode",
                Platform.isDevelopmentEnvironment(),
                "Development mode of Miapi - DO NOT ENABLE IF U DONT KNOW WHAT IT DOES");
        public static BooleanConfigItem forgeAutoReloads = new BooleanConfigItem(
                "forge_reload_mode",
                Platform.isForge(),
                "Auto reloads on Servers to fix Forge having buggy classloading");

        protected OtherConfigGroup() {
            super(of(developmentMode, forgeAutoReloads), "other");
        }
    }

    public static class EnchantmentGroup extends ConfigItemGroup {
        public static BooleanConfigItem betterInfinity = new BooleanConfigItem(
                "better_infinity",
                true,
                "Modular Bows no longer require any arrows with infinity");
        public static BooleanConfigItem betterLoyalty = new BooleanConfigItem(
                "better_loyalty",
                true,
                "Loyalty triggers in the void with modular Items");
        public static BooleanConfigItem leanEnchantments = new BooleanConfigItem(
                "lean_enchantments",
                true,
                "Truly Modular is more lean with its allowed Enchantments on its Tools");

        protected EnchantmentGroup() {
            super(of(betterInfinity, betterLoyalty, leanEnchantments), "enchants");
        }
    }
}
