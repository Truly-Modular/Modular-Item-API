package smartin.miapi.forge.compat;

public class QuarkCompat {

    public static void setup() {
        /*
        GlintProperty.GLINT_RESOLVE.register(new GlintProperty.GlintGetter() {
            @Override
            public EventResult get(ItemStack itemStack, ItemModule.ModuleInstance moduleInstance, AtomicReference<GlintProperty.GlintSettings> currentSettings) {
                RuneColor runeColor = ColorRunesModule.getStackColor(itemStack);
                if (runeColor != null) {
                    float alpha = runeColor.getDyeColor() != null ? 0.5f : 0.0f;
                    Color color = runeColor.getDyeColor() != null ? Color.fromRgbInt(runeColor.getDyeColor().getSignColor()) : Color.BLACK;
                    if (runeColor.equals(RuneColor.RAINBOW)) {
                        currentSettings.set(
                                new GlintProperty.RainbowGlintSettings(
                                        0.5f,1.5f,
                                        0.5f,true,
                                        Color.RAINBOW) {
                            @Override
                            public GlintProperty.GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack) {
                                return this;
                            }
                        });
                    } else {
                        GlintProperty.GlintSettings glintSettings = new GlintProperty.GlintSettings() {
                            @Override
                            public GlintProperty.GlintSettings get(ItemModule.ModuleInstance instance, ItemStack stack) {
                                return this;
                            }

                            @Override
                            public float getA() {
                                return alpha;
                            }

                            @Override
                            public Color getColor() {
                                return color;
                            }

                            @Override
                            public float getSpeed() {
                                return 0.5f;
                            }

                            @Override
                            public boolean shouldRender() {
                                return true;
                            }
                        };
                        currentSettings.set(glintSettings);
                    }
                }
                return EventResult.pass();
            }
        });

         */
    }
}
