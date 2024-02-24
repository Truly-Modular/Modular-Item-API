package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.GuiWidgetSupplier;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * This property controls {@link smartin.miapi.modules.abilities.HeavyAttackAbility}
 * will be replaced by {@link smartin.miapi.modules.abilities.HeavyAttackAbility}
 */
@Deprecated
public class HeavyAttackProperty extends CodecBasedProperty<HeavyAttackProperty.HeavyAttackHolder> implements GuiWidgetSupplier {
    public static final String KEY = "heavyAttack";
    public static HeavyAttackProperty property;
    public static final Codec<HeavyAttackHolder> codec = AutoCodec.of(HeavyAttackHolder.class).codec();

    public HeavyAttackProperty() {
        super(KEY, codec);
        property = this;
    }


    public HeavyAttackHolder get(ItemStack itemStack){
        AbilityMangerProperty.AbilityContext context = AbilityMangerProperty.getContext(itemStack, KEY);
        if (context != null && context.isFullContext) {
            return codec.parse(JsonOps.INSTANCE,context.contextJson).result().get();
        }
        return super.get(itemStack);
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case EXTEND, SMART -> {
                return old.deepCopy();
            }
        }
        return old.deepCopy();
    }

    public boolean hasHeavyAttack(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public StatListWidget.TextGetter getTitle() {
        return (stack -> {
            HeavyAttackHolder json = get(stack);
            if (json != null) {
                return Text.translatable(json.title);
            }
            return Text.empty();
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public StatListWidget.TextGetter getDescription() {
        return (stack -> {
            HeavyAttackHolder json = get(stack);
            if (json != null) {
                return Text.translatable(json.description, json.damage, json.range, json.minHold / 20);
            }
            return Text.empty();
        });
    }

    @Environment(EnvType.CLIENT)
    @Override
    public SingleStatDisplayDouble.StatReaderHelper getStatReader() {
        return new SingleStatDisplayDouble.StatReaderHelper() {
            @Override
            public double getValue(ItemStack itemStack) {
                HeavyAttackHolder json = get(itemStack);
                if (json != null) {
                    return json.range;
                }
                return 0;
            }

            @Override
            public boolean hasValue(ItemStack itemStack) {
                return get(itemStack) != null;
            }
        };
    }

    public static class HeavyAttackHolder {
        public double damage = 1.0;
        public double sweeping = 0.0;
        public double range = 3.5;
        public double minHold = 20;
        public double cooldown = 20;
        @CodecBehavior.Optional
        public String title = "miapi.ability.heavy_attack.title";
        @CodecBehavior.Optional
        public String description = "miapi.ability.heavy_attack.description";
        @CodecBehavior.Optional
        public ParticleEffect particleEffect;
    }
}
