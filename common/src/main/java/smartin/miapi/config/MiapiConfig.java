package smartin.miapi.config;

import dev.architectury.platform.Platform;
import smartin.miapi.config.oro_config.BooleanConfigItem;
import smartin.miapi.config.oro_config.Config;
import smartin.miapi.config.oro_config.ConfigItemGroup;

import java.io.File;
import java.util.List;

import static org.spongepowered.include.com.google.common.collect.ImmutableList.of;

public class MiapiConfig extends Config {
    public static EnchantmentGroup enchantmentGroup = new EnchantmentGroup();
    public static OtherConfigGroup otherGroup = new OtherConfigGroup();
    protected static MiapiConfig INSTANCE = new MiapiConfig();

    public static MiapiConfig getInstance(){
        return INSTANCE;
    }

    protected MiapiConfig() {
        super(List.of(otherGroup,enchantmentGroup),
                new File(Platform.getConfigFolder().toString(),"miapi.json"),
                "miapi_server");
        if(Platform.isModLoaded("cloth_config")){
        }
        this.saveConfigToFile();
    }

    public static class OtherConfigGroup extends ConfigItemGroup{
        public static BooleanConfigItem developmentMode = new BooleanConfigItem(
                "development_mode",
                Platform.isDevelopmentEnvironment(),
                "Development mode of Miapi - DO NOT ENABLE IF U DONT KNOW WHAT IT DOES");
        protected OtherConfigGroup() {
            super(of(developmentMode), "other");
        }
    }

    public static class EnchantmentGroup extends ConfigItemGroup{
        public static BooleanConfigItem betterInfinity = new BooleanConfigItem(
                "better_infinity",
                true,
                "Modular Bows no longer require any arrows with infinity");
        public static BooleanConfigItem betterLoyalty = new BooleanConfigItem(
                "better_loyalty",
                true,
                "Loyalty triggers in the void with modular Items");
        protected EnchantmentGroup() {
            super(of(betterInfinity,betterLoyalty), "enchants");
        }
    }
}
