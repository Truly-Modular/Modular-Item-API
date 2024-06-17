package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.misc.MiscCodecs;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.event.EventResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This property manages the Glint on the item
 */
public class GlintProperty implements ModuleProperty {
    //TODO:add gui implementation
    public static GlintProperty property;
    public static final String KEY = "glint_settings";

    public static Map<String, GlintSettings> glintSettingsMap = new HashMap<>();
    public static PrioritizedEvent<GlintGetter> GLINT_RESOLVE = PrioritizedEvent.createLoop();


    public static GlintSettings defaultSettings = new SettingsControlledGlint();

    public GlintProperty() {
        property = this;
        glintSettingsMap.put("rainbow", new SettingsControlledGlint());
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    public GlintSettings getGlintSettings(ModuleInstance instance, ItemStack stack) {
        return getStatic(instance, stack);
    }

    static GlintSettings getStatic(ModuleInstance instance, ItemStack stack) {
        JsonElement element = instance.getProperties().get(property);
        if (element != null && element.getAsJsonObject().has("type")) {
            String type = element.getAsJsonObject().get("type").getAsString();
            if (glintSettingsMap.containsKey(type)) {
                return glintSettingsMap.get("type").get(instance, stack);
            }
        }
        AtomicReference<GlintSettings> reference = new AtomicReference<>(defaultSettings);
        GLINT_RESOLVE.invoker().get(stack, instance, reference);
        return reference.get();
    }


    public static class JsonGlintSettings implements GlintSettings {
        public Color color;
        public float a = 1;
        public float speed = 1;
        public boolean shouldRenderGlint;

        public JsonGlintSettings(ModuleInstance instance, ItemStack stack) {
            shouldRenderGlint = stack.hasGlint();
            JsonElement element = instance.getProperties().get(property);
            if (element != null) {
                if (element.getAsJsonObject().has("color")) {
                    try{
                        this.color = MiscCodecs.COLOR.parse(JsonOps.INSTANCE, element.getAsJsonObject().get("color")).getOrThrow();
                    }catch (Exception e){
                        Miapi.LOGGER.error("Failed to decode using color for GlintProperty! -> " + e);
                        this.color = Color.BLACK;
                    }
                    a = color.a() * 2;
                }
                if (element.getAsJsonObject().has("speed")) {
                    speed = (float) StatResolver.resolveDouble(element.getAsJsonObject().get("speed"), instance);
                }
                if (element.getAsJsonObject().has("should_render")) {
                    shouldRenderGlint = element.getAsJsonObject().get("should_render").getAsBoolean();
                }
            }
        }

        @Override
        public GlintSettings get(ModuleInstance instance, ItemStack stack) {
            JsonElement element = instance.getProperties().get(property);
            if (element.getAsJsonObject().has("type")) {
                String type = element.getAsJsonObject().get("type").getAsString();
                if (glintSettingsMap.containsKey(type)) {
                    return glintSettingsMap.get("type").get(instance, stack);
                }
            }
            return new JsonGlintSettings(instance, stack);
        }

        public float getA() {
            return a;
        }

        @Override
        public Color getColor() {
            return color;
        }


        public float getSpeed() {
            return speed;
        }

        public boolean shouldRender() {
            return shouldRenderGlint;
        }
    }

    public static void updateConfig() {
        Color[] newColors = new Color[MiapiConfig.INSTANCE.client.other.enchantColors.size()];
        for (int i = 0; i < newColors.length; i++) {
            newColors[i] = MiapiConfig.INSTANCE.client.other.enchantColors.get(i);
        }
        SettingsControlledGlint glintSettings = new SettingsControlledGlint();
        glintSettings.colors = newColors;
        glintSettings.rainbowSpeed = MiapiConfig.INSTANCE.client.other.enchantingGlintSpeed;
        defaultSettings = glintSettings;
        ModularItemCache.discardCache();
    }


    public static class SettingsControlledGlint extends RainbowGlintSettings {

        public SettingsControlledGlint() {
            super();
        }

        @Override
        public GlintSettings get(ModuleInstance instance, ItemStack stack) {
            JsonElement element = instance.getProperties().get(property);
            SettingsControlledGlint rainbowGlintSettings = new SettingsControlledGlint();
            rainbowGlintSettings.shouldRenderGlint = stack.hasGlint();
            if (element != null) {
                if (element.getAsJsonObject().has("rainbowSpeed")) {
                    rainbowGlintSettings.rainbowSpeed = (float) StatResolver.resolveDouble(element.getAsJsonObject().get("rainbowSpeed"), instance);
                }
                if (element.getAsJsonObject().has("speed")) {
                    rainbowGlintSettings.speed = (float) StatResolver.resolveDouble(element.getAsJsonObject().get("speed"), instance);
                }
                if (element.getAsJsonObject().has("strength")) {
                    rainbowGlintSettings.strength = (float) StatResolver.resolveDouble(element.getAsJsonObject().get("strength"), instance);
                }
                if (element.getAsJsonObject().has("should_render")) {
                    rainbowGlintSettings.shouldRenderGlint = element.getAsJsonObject().get("should_render").getAsBoolean();
                }
            }
            return rainbowGlintSettings;
        }
    }

    public static abstract class RainbowGlintSettings implements GlintSettings {

        public float speed = 1;
        public float rainbowSpeed = 1;
        public float strength = 1;
        public boolean shouldRenderGlint;
        public Color[] colors;

        public RainbowGlintSettings(float speed, float rainbowSpeed, float strength, boolean shouldRenderGlint, Color[] colors) {
            this.speed = speed;
            this.rainbowSpeed = rainbowSpeed;
            this.strength = strength;
            this.shouldRenderGlint = shouldRenderGlint;
            this.colors = colors;
        }

        public RainbowGlintSettings() {
        }

        @Override
        public float getA() {
            return strength;
        }


        public Color getColorold() {
            long time = Util.getMeasuringTimeMs();
            double scaledTime = (double) time / 3000 * rainbowSpeed;
            scaledTime = scaledTime % (colors.length); // Ensure scaledTime is within [0, colors.length - 1]

            int lowerColorIndex = (int) Math.floor(scaledTime);
            int higherColorIndex = (lowerColorIndex + 1) % (colors.length);

            float percent = (float) (scaledTime - (float) lowerColorIndex); // Calculate the percentage of lower color
            float otherPercent = 1.0f - percent;

            return new Color(
                    colors[lowerColorIndex].redAsFloat() * percent + colors[higherColorIndex].redAsFloat() * otherPercent,
                    colors[lowerColorIndex].greenAsFloat() * percent + colors[higherColorIndex].greenAsFloat() * otherPercent,
                    colors[lowerColorIndex].blueAsFloat() * percent + colors[higherColorIndex].blueAsFloat() * otherPercent,
                    colors[lowerColorIndex].alphaAsFloat() * percent + colors[higherColorIndex].alphaAsFloat() * otherPercent
            );
        }

        @Override
        public Color getColor() {
            long time = Util.getMeasuringTimeMs();

            double scaledTime = (double) time / 3000 * rainbowSpeed;
            scaledTime = scaledTime % (colors.length); // Ensure scaledTime is within [0, colors.length - 1]

            int lowerColorIndex = (int) Math.floor(scaledTime);
            int higherColorIndex = (lowerColorIndex + 1) % colors.length;

            // Calculate the percentage of lower color
            float percent = (float) (scaledTime - Math.floor(scaledTime));
            float otherPercent = 1.0f - percent;

            // Interpolate between the two nearest colors
            Color lowerColor = colors[higherColorIndex];
            Color higherColor = colors[lowerColorIndex];

            return new Color(
                    lowerColor.redAsFloat() * percent + higherColor.redAsFloat() * otherPercent,
                    lowerColor.greenAsFloat() * percent + higherColor.greenAsFloat() * otherPercent,
                    lowerColor.blueAsFloat() * percent + higherColor.blueAsFloat() * otherPercent,
                    lowerColor.alphaAsFloat() * percent + higherColor.alphaAsFloat() * otherPercent
            );
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        public float getColor(int colorNo) {
            long time = Util.getMeasuringTimeMs();
            double scaledTime = (double) time / 3000 * rainbowSpeed;
            return (float) Math.max(0, Math.min(1, Math.abs(((scaledTime + colorNo * 2) % (colors.length * 2)) - colors.length) - (colors.length - 2)));
        }

        @Override
        public boolean shouldRender() {
            return shouldRenderGlint;
        }
    }

    public interface GlintSettings {

        GlintSettings get(ModuleInstance instance, ItemStack stack);

        float getA();

        Color getColor();

        float getSpeed();

        default void applySpeed() {
            RegistryInventory.Client.glintShader.getUniformOrDefault("GlintSpeed").set(getSpeed());
        }

        default void applyAlpha() {
            RegistryInventory.Client.glintShader.getUniformOrDefault("GlintStrength").set(getA());
        }

        boolean shouldRender();
    }

    public interface GlintGetter {
        EventResult get(ItemStack itemStack, ModuleInstance moduleInstance, AtomicReference<GlintSettings> currentSettings);
    }
}
