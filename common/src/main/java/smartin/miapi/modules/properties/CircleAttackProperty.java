package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This property controls {@link smartin.miapi.modules.abilities.CircleAttackAbility}
 */
@Deprecated
public class CircleAttackProperty implements ModuleProperty {
    //TODO:Delete this class and think of a better Solution for special attacks
    public static String KEY = "circleAttack";
    public static CircleAttackProperty property;

    public CircleAttackProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        new CircleAttackJson(data, new ModuleInstance(ItemModule.empty));
        return true;
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

    public CircleAttackJson get(ItemStack itemStack) {
        JsonElement jsonElement = ItemModule.getMergedProperty(itemStack, property);
        if (jsonElement == null) {
            return null;
        }
        return Miapi.gson.fromJson(jsonElement, CircleAttackJson.class);
    }

    public boolean hasCircleAttack(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    public static class CircleAttackJson {
        public double damage;
        public double range;
        public double minHold;
        public double cooldown;
        public List<ParticleJson> particles = new ArrayList<>();

        public CircleAttackJson(JsonElement element, ModuleInstance instance) {
            JsonObject object = element.getAsJsonObject();
            damage = get(object.get("damage"), instance);
            range = get(object.get("range"), instance);
            minHold = get(object.get("minHold"), instance);
            cooldown = get(object.get("cooldown"), instance);
            if (object.has("particles")) {
                TypeToken<List<ParticleJson>> typeToken = new TypeToken<>() {
                };
                Type listType = typeToken.getType();

                particles = Miapi.gson.fromJson(object.get("particles"), listType);
                particles.forEach(particleJson -> {
                    particleJson.particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(particleJson.particle));
                });
            }
        }

        private double get(JsonElement object, ModuleInstance instance) {
            try {
                return object.getAsDouble();
            } catch (Exception e) {
                return StatResolver.resolveDouble(object.getAsString(), instance);
            }
        }

        public static class ParticleJson {
            public String particle;
            public ParticleType particleType;
            public int count;
            public double rangePercent;
        }

    }
}
