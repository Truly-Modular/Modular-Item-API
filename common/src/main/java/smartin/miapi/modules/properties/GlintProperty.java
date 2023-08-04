package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.datapack.codec.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class GlintProperty implements ModuleProperty {
    public static GlintProperty property;
    public static final String KEY = "glint_settings";

    public static Map<String, GlintSettings> glintSettingsMap = new HashMap<>();

    public GlintProperty() {
        property = this;
        glintSettingsMap.put("rainbow", new RainbowGlintSettings());
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    public GlintSettings getGlintSettings(ItemModule.ModuleInstance instance, ItemStack stack) {
        return getStatic(instance, stack);
    }

    static GlintSettings getStatic(ItemModule.ModuleInstance instance, ItemStack stack) {
        if (true) {
            ItemRenderer renderer;
            return glintSettingsMap.get("rainbow").get(instance, stack);
        }
        JsonElement element = instance.getProperties().get(property);
        if (element != null && element.getAsJsonObject().has("type")) {
            String type = element.getAsJsonObject().get("type").getAsString();
            if (glintSettingsMap.containsKey(type)) {
                return glintSettingsMap.get("type").get(instance, stack);
            }
        }
        return new JsonGlintSettings(instance, stack);
    }


    public static class JsonGlintSettings implements GlintSettings {
        public Color color;
        public float a = 1;
        public float speed = 1;
        public boolean shouldRender;

        public JsonGlintSettings(ItemModule.ModuleInstance instance, ItemStack stack) {
            shouldRender = stack.hasGlint();
            JsonElement element = instance.getProperties().get(property);
            if (element != null) {
                if (element.getAsJsonObject().has("color")) {
                    Color color = MiscCodecs.COLOR.parse(JsonOps.INSTANCE, element.getAsJsonObject().get("color")).getOrThrow(false, s -> {
                        Miapi.LOGGER.error("Failed to decode using color for GlintProperty! -> " + s);
                    });
                    this.color = color;
                    a = color.a * 2;
                }
                if (element.getAsJsonObject().has("speed")) {
                    speed = (float) StatResolver.resolveDouble(element.getAsJsonObject().get("speed"), instance);
                }
                if (element.getAsJsonObject().has("should_render")) {
                    shouldRender = element.getAsJsonObject().get("should_render").getAsBoolean();
                }
            }
        }

        @Override
        public GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack) {
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
            return shouldRender;
        }
    }


    public static class RainbowGlintSettings implements GlintSettings {

        public float speed = 1;
        public float rainbowSpeed = 1;
        public float strength = 1;
        public boolean shouldRender;
        int colorCount = 3;

        @Override
        public GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack) {
            JsonElement element = instance.getProperties().get(property);
            RainbowGlintSettings rainbowGlintSettings = new RainbowGlintSettings();
            rainbowGlintSettings.shouldRender = stack.hasGlint();
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
                    rainbowGlintSettings.shouldRender = element.getAsJsonObject().get("should_render").getAsBoolean();
                }
            }
            return rainbowGlintSettings;
        }

        @Override
        public float getA() {
            return strength;
        }

        @Override
        public Color getColor() {
            return new Color((int) (getColor(0)*255), (int) (getColor(1)*255), (int) (getColor(2)*255), 1);
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        public float getColor(int colorNo) {
            long time = Util.getMeasuringTimeMs();
            double scaledTime = (double) time / 3000 * rainbowSpeed;
            return (float) Math.max(0, Math.min(1, Math.abs(((scaledTime + colorNo * 2) % (colorCount * 2)) - colorCount) - (colorCount - 2)));
        }

        @Override
        public boolean shouldRender() {
            return shouldRender;
        }
    }

    public interface GlintSettings {

        GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack);

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
}
