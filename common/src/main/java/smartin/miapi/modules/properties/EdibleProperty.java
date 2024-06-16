package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.util.json.JsonUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class EdibleProperty implements ModuleProperty {
    //@Panda is this done or under construction still?
    public static final String KEY = "edible";
    public static EdibleProperty property;

    public EdibleProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, EdibleProperty::createCache);
    }

    public static Holder get(ItemStack itemStack) {
        return (Holder) ModularItemCache.getRaw(itemStack, KEY);
    }

    public static Holder createCache(ItemStack stack) {
        Holder result = new Holder(0, 0, 1,0, false, new ArrayList<>());
        for (ModuleInstance subModule : ItemModule.getModules(stack).allSubModules()) {
            JsonElement element = subModule.getProperties().get(property);
            if (element == null) continue;

            Holder holder = RawData.codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
                Miapi.LOGGER.error("Failed to decode using codec during cache creation for a CodecBasedProperty! -> " + s);
            }).toHolder(subModule);

            result.hunger += holder.hunger;
            result.saturation += holder.saturation;
            result.eatingSpeed *= holder.eatingSpeed;
            result.effects.addAll(holder.effects);
            result.durabilityDamage += holder.durabilityDamage;
            if (holder.alwaysEdible) result.alwaysEdible = true;
        }

        return result;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        RawData.codec.parse(JsonOps.INSTANCE, data).getOrThrow(false, s -> {
            Miapi.LOGGER.error("Failed to parse data for edible property for module with key '{}'! -> {}", moduleKey, s);
            throw new RuntimeException();
        });
        return true;
    }

    @Override
    public JsonElement merge(JsonElement oldEl, JsonElement toMergeEl, MergeType type) {
        if (type == MergeType.OVERWRITE) return toMergeEl.deepCopy();

        JsonObject newElement = new JsonObject();
        JsonObject old = oldEl.getAsJsonObject();
        JsonObject toMerge = toMergeEl.getAsJsonObject();

        newElement.addProperty("hunger", JsonUtil.getOrElse(old, "hunger", "0") + "+" + JsonUtil.getOrElse(toMerge, "hunger", "0"));
        newElement.addProperty("saturation", JsonUtil.getOrElse(old, "saturation", "0") + "+" + JsonUtil.getOrElse(toMerge, "saturation", "0"));
        newElement.addProperty("eatingSpeed", JsonUtil.getOrElse(old, "eatingSpeed", "0") + "+" + JsonUtil.getOrElse(toMerge, "eatingSpeed", "0"));

        JsonElement oldEdibility = old.get("alwaysEdible");
        JsonElement toMergeEdibility = toMerge.get("alwaysEdible");
        newElement.addProperty("alwaysEdible", (oldEdibility != null && oldEdibility.getAsBoolean()) || (toMergeEdibility != null && toMergeEdibility.getAsBoolean()));

        JsonElement oldEffects = old.get("effects");
        JsonElement toMergeEffects = toMerge.get("effects");
        JsonArray newEffects = new JsonArray();
        if (oldEffects != null) newEffects.addAll(oldEffects.getAsJsonArray());
        if (toMergeEffects != null) newEffects.addAll(toMergeEffects.getAsJsonArray());
        newElement.add("effects", newEffects);

        return newElement;
    }

    public static class RawData {
        public static final Codec<RawData> codec = AutoCodec.of(RawData.class).codec();

        public StatResolver.IntegerFromStat hunger; // todo perhaps make these number values similar to DoubleProperty's operation?
        public StatResolver.DoubleFromStat saturation;
        public @CodecBehavior.Optional StatResolver.DoubleFromStat durability = new StatResolver.DoubleFromStat(0);
        public @CodecBehavior.Optional StatResolver.DoubleFromStat eatingSpeed = new StatResolver.DoubleFromStat(1);
        public @CodecBehavior.Optional boolean alwaysEdible = false;
        public @CodecBehavior.Optional List<StatusEffectHolder> effects = new ArrayList<>();

        public Holder toHolder(ModuleInstance instance) {
            return new Holder(
                    hunger.evaluate(instance),
                    saturation.evaluate(instance),
                    eatingSpeed.evaluate(instance),
                    durability.evaluate(instance),
                    alwaysEdible,
                    effects.stream().map(e -> new StatusEffectInstance(e.effect, e.duration, e.amplifier, e.ambient, e.showParticles, e.showIcon)).toList());
        }
    }

    public static final class Holder {
        public int hunger;
        public double saturation;
        public double eatingSpeed;
        public double durabilityDamage;
        public boolean alwaysEdible;
        public List<StatusEffectInstance> effects;

        public Holder(int hunger, double saturation, double eatingSpeed, double durabilityDamage, boolean alwaysEdible, List<StatusEffectInstance> effects) {
            this.hunger = hunger;
            this.durabilityDamage = durabilityDamage;
            this.saturation = saturation;
            this.eatingSpeed = eatingSpeed;
            this.alwaysEdible = alwaysEdible;
            this.effects = effects;
        }

        public void finishedEat(ItemStack itemStack, Random random, ServerPlayerEntity serverPlayerEntity) {
            if (this.durabilityDamage == 0) {
                itemStack.decrement(1);
            } else {
                itemStack.damage((int) durabilityDamage, random, serverPlayerEntity);
            }
        }
    }

    @CodecBehavior.Override("codec")
    @AutoCodec.Settings(defaultOptionalBehavior = @CodecBehavior.Optional)
    public static class StatusEffectHolder {
        public static final Codec<StatusEffectHolder> codec = AutoCodec.of(StatusEffectHolder.class).codec();

        public @CodecBehavior.Optional(false) StatusEffect effect;
        public @CodecBehavior.Optional(false) int duration;
        public @CodecBehavior.Optional(false) int amplifier;
        public boolean ambient = false;
        public boolean showParticles = true;
        public boolean showIcon = true;
    }
}
