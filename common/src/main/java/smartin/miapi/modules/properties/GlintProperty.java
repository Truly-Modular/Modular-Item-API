package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.event.PrioritizedEvent;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.event.EventResult;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.GlintShader;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This property manages the Glint on the item
 */
public class GlintProperty extends CodecProperty<GlintProperty.RainbowGlintSettings> {
    public static GlintProperty property;
    public static final ResourceLocation KEY = Miapi.id("glint_settings");
    public static PrioritizedEvent<GlintGetter> GLINT_RESOLVE = PrioritizedEvent.createLoop();
    public static SettingsControlledGlint defaultSettings = new SettingsControlledGlint();
    public static final Codec<RainbowGlintSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("speed", 1.0f).forGetter(settings -> settings.speed),
            Codec.FLOAT.optionalFieldOf("rainbow_speed", 1.0f).forGetter(settings -> settings.rainbowSpeed),
            Codec.FLOAT.optionalFieldOf("strength", 1.0f).forGetter(settings -> settings.strength),
            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("should_render_glint", false).forGetter(settings -> settings.shouldRenderGlint),
            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("is_item", false).forGetter(settings -> settings.isItem),
            Codec.list(RainbowGlintSettings.COLOR_CODEC).optionalFieldOf("colors", List.of(Color.WHITE)).forGetter(settings -> Arrays.asList(settings.colors))
    ).apply(instance, (speed, rainbowSpeed, strength, shouldRenderGlint, item, colors) -> {
        RainbowGlintSettings settings = new RainbowGlintSettings();
        settings.speed = speed;
        settings.rainbowSpeed = rainbowSpeed;
        settings.strength = strength;
        settings.shouldRenderGlint = shouldRenderGlint;
        settings.colors = colors.toArray(new Color[0]);
        settings.isItem = item;
        return settings;
    }));
    public static RainbowGlintSettings vanillaLike = new RainbowGlintSettings();

    public GlintProperty() {
        super(CODEC);
        property = this;
    }

    public GlintSettings getGlintSettings(ModuleInstance instance, ItemStack stack) {

        AtomicReference<GlintSettings> reference = new AtomicReference<>(getData(instance).or(() -> {
            var stackData = getData(stack);
            if (stackData.isPresent() && stackData.get().isItem) {
                return stackData;
            }
            return Optional.empty();
        }).orElseGet(() -> {
            if (MiapiConfig.INSTANCE.client.enchantingGlint.vanillaLike) {
                return vanillaLike;
            }
            if (MaterialProperty.getMaterial(instance) != null) {
                Color adjusted = new Color(MaterialProperty.getMaterial(instance).getColor(instance, ItemDisplayContext.GUI));
                return defaultSettings.copyWithColor(adjustWith(
                        adjusted,
                        MiapiConfig.INSTANCE.client.enchantingGlint.materialRatioColor,
                        defaultSettings.colors));
            }
            return defaultSettings;
        }));
        GLINT_RESOLVE.invoker().get(stack, instance, reference);
        return reference.get();
    }

    public List<Color> adjustWith(Color adjust, float percent, Color[] previous) {
        List<Color> adjusted = new ArrayList<>();
        for (int i = 0; i < previous.length; i++) {
            Color c = previous[i].copy();
            c.lerp(percent, adjust);
            adjusted.add(i, c);
        }
        return adjusted;
    }

    public static void updateConfig() {
        Color[] newColors = new Color[MiapiConfig.INSTANCE.client.enchantingGlint.enchantColors.size()];
        for (int i = 0; i < newColors.length; i++) {
            newColors[i] = MiapiConfig.INSTANCE.client.enchantingGlint.enchantColors.get(i);
        }
        if (newColors.length == 0) {
            newColors = new Color[]{Color.WHITE};
        }
        SettingsControlledGlint glintSettings = new SettingsControlledGlint();
        glintSettings.colors = newColors;
        glintSettings.rainbowSpeed = MiapiConfig.INSTANCE.client.enchantingGlint.enchantingGlintSpeed;
        defaultSettings = glintSettings;
        ModularItemCache.discardCache();
    }

    @Override
    public RainbowGlintSettings merge(RainbowGlintSettings left, RainbowGlintSettings right, MergeType mergeType) {
        if (left.isItem) {
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
        public static Codec<Color> COLOR_CODEC = Codec.INT.xmap(Color::new, Color::abgr);
        public float speed = 1;
        public float rainbowSpeed = 1;
        public float strength = 1;
        public boolean shouldRenderGlint = false;
        public boolean isItem = false;
        public Color[] colors = new Color[]{new Color("A755FF80")};

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

        public RainbowGlintSettings copyWithColor(List<Color> newColors) {
            RainbowGlintSettings copy = new RainbowGlintSettings();
            copy.speed = this.speed;
            copy.rainbowSpeed = this.rainbowSpeed;
            copy.strength = this.strength;
            copy.shouldRenderGlint = this.shouldRenderGlint;
            copy.colors = newColors.toArray(new Color[0]);
            return copy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RainbowGlintSettings that = (RainbowGlintSettings) o;
            return Float.compare(that.speed, speed) == 0 &&
                   Float.compare(that.rainbowSpeed, rainbowSpeed) == 0 &&
                   Float.compare(that.strength, strength) == 0 &&
                   shouldRenderGlint == that.shouldRenderGlint &&
                   Arrays.equals(colors, that.colors);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(speed, rainbowSpeed, strength, shouldRenderGlint);
            result = 31 * result + Arrays.hashCode(colors);
            return result;
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
