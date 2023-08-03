package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.datapack.codec.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.Map;

public class GlintProperty implements ModuleProperty {
    public static GlintProperty property;
    public static final String KEY = "glint_settings";

    public static Map<String, GlintSettings> glintSettingsMap = new HashMap<>();

    public GlintProperty() {
        property = this;
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
            return new RainbowGlintSettings();
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
        public float r = 1;
        public float g = 1;
        public float b = 1;
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
                    r = color.r;
                    g = color.g;
                    b = color.b;
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

        public float getB() {
            return b;
        }

        public float getG() {
            return g;
        }

        public float getR() {
            return r;
        }

        public float getSpeed() {
            return speed;
        }

        public boolean shouldRender() {
            return shouldRender;
        }
    }

    public static class RainbowGlintSettings implements GlintSettings {
        @Override
        public GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack) {
            return new RainbowGlintSettings();
        }

        @Override
        public float getA() {
            Long time = Util.getMeasuringTimeMs();
            float alpha = (float) Math.sin(((float) time) / 3000);
            return 1;
        }

        @Override
        public float getB() {
            Long time = Util.getMeasuringTimeMs();
            float alpha = (float) Math.sin(((float) time + Math.PI / 4) / 4000) * 0.5f + 0.5f;
            return alpha;
        }

        @Override
        public float getG() {
            Long time = Util.getMeasuringTimeMs();
            float alpha = (float) Math.sin(((float) time + Math.PI / 4 * 3) / 5000) * 0.5f + 0.5f;
            return alpha;
        }

        @Override
        public float getR() {
            Long time = Util.getMeasuringTimeMs();
            float alpha = (float) Math.sin(((float) time) / 3000) * 0.5f + 0.5f;
            return alpha;
        }

        @Override
        public float getSpeed() {
            Long time = Util.getMeasuringTimeMs();
            float alpha = (float) Math.sin(((float) time) / 10000);
            return 1;
        }

        @Override
        public boolean shouldRender() {
            return true;
        }
    }

    public interface GlintSettings {

        GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack);

        float getA();

        float getB();

        float getG();

        float getR();

        float getSpeed();

        boolean shouldRender();
    }
}
