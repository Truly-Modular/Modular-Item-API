package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.event.EventResult;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.client.GlintShader;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This property manages the Glint on the item
 */
public class GlintProperty extends CodecProperty<GlintProperty.RainbowGlintSettings> {
    public static GlintProperty property;
    public static final String KEY = "glint_settings";
    public static PrioritizedEvent<GlintGetter> GLINT_RESOLVE = PrioritizedEvent.createLoop();
    public static SettingsControlledGlint defaultSettings = new SettingsControlledGlint();
    public static Codec<RainbowGlintSettings> CODEC = AutoCodec.of(RainbowGlintSettings.class).codec();

    public GlintProperty() {
        super(CODEC);
        property = this;
        updateConfig();
    }

    public GlintSettings getGlintSettings(ModuleInstance instance, ItemStack stack) {
        AtomicReference<GlintSettings> reference = new AtomicReference<>(getData(instance).orElse(defaultSettings));
        GLINT_RESOLVE.invoker().get(stack, instance, reference);
        return reference.get();
    }

    public static void updateConfig() {
        Color[] newColors = new Color[MiapiConfig.INSTANCE.client.other.enchantColors.size()];
        for (int i = 0; i < newColors.length; i++) {
            newColors[i] = MiapiConfig.INSTANCE.client.other.enchantColors.get(i);
        }
        if (newColors.length == 0) {
            newColors = new Color[]{Color.WHITE};
        }
        SettingsControlledGlint glintSettings = new SettingsControlledGlint();
        glintSettings.colors = newColors;
        glintSettings.rainbowSpeed = MiapiConfig.INSTANCE.client.other.enchantingGlintSpeed;
        defaultSettings = glintSettings;
        ModularItemCache.discardCache();
    }

    @Override
    public RainbowGlintSettings merge(RainbowGlintSettings left, RainbowGlintSettings right, MergeType mergeType) {
        if (mergeType.equals(MergeType.EXTEND)) {
            return left;
        }
        return right;
    }


    public static class SettingsControlledGlint extends RainbowGlintSettings {
        public SettingsControlledGlint() {
            super();
        }
    }

    public static class RainbowGlintSettings implements GlintSettings {
        @CodecBehavior.Optional
        public float speed = 1;
        @CodecBehavior.Optional
        public float rainbowSpeed = 1;
        @CodecBehavior.Optional
        public float strength = 1;
        @CodecBehavior.Optional
        public boolean shouldRenderGlint = false;
        @CodecBehavior.Optional
        public Color[] colors = new Color[]{Color.WHITE};

        @Override
        public float getA() {
            return strength;
        }

        @Override
        public Color getColor() {
            long time = Util.getMillis();

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
            long time = Util.getMillis();
            double scaledTime = (double) time / 3000 * rainbowSpeed;
            return (float) Math.max(0, Math.min(1, Math.abs(((scaledTime + colorNo * 2) % (colors.length * 2)) - colors.length) - (colors.length - 2)));
        }

        @Override
        public boolean shouldRender() {
            return shouldRenderGlint;
        }
    }

    public interface GlintSettings {
        float getA();

        Color getColor();

        float getSpeed();

        default void applySpeed() {
            GlintShader.glintShader.safeGetUniform("GlintSpeed").set(getSpeed());
        }

        default void applyAlpha() {
            GlintShader.glintShader.safeGetUniform("GlintStrength").set(getA());
        }

        boolean shouldRender();
    }

    public interface GlintGetter {
        EventResult get(ItemStack itemStack, ModuleInstance moduleInstance, AtomicReference<GlintSettings> currentSettings);
    }
}
