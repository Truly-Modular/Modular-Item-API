package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import smartin.miapi.events.Event;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PotionEffectProperty implements ModuleProperty {
    public static String KEY = "applyPotionEffects";
    public static PotionEffectProperty property;

    public PotionEffectProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, PotionEffectProperty::createCache);
        Event.LIVING_HURT_AFTER.register(this::handleEvent);
    }

    public EventResult handleEvent(Event.LivingHurtEvent event) {
        LivingEntity victim = event.livingEntity;
        if (!(event.damageSource.getAttacker() instanceof LivingEntity attacker) || victim.world.isClient) return EventResult.pass();

        if (attacker instanceof PlayerEntity) {
            System.out.println(victim.getAttacker());
            System.out.println(attacker.getAttacking());
        }

        List<StatusEffectData> effects = ofEntity(victim); // setting up and merging effect data
        effects.addAll(ofEntity(attacker).stream()
                .map(d -> new StatusEffectData(d.event.inverse(), d.target.inverse(), d.creator, d.effect, d.duration, d.amplifier, d.ambient, d.visible, d.showIcon)) // inverting
                .toList()
        );

        for (StatusEffectData effect : effects) {
            if (effect.event == ApplicationEvent.HURT) {
                LivingEntity toApply = victim;
                if (effect.target == ApplicationTarget.TARGET)
                    toApply = attacker;

                toApply.addStatusEffect(effect.creator.get());
            }
        }

        return EventResult.pass();
    }

    public List<StatusEffectData> ofEntity(LivingEntity entity) {
        List<StatusEffectData> list = property.get(entity.getMainHandStack());
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        StatusEffectData.CODEC(new ItemModule.ModuleInstance(ItemModule.empty)).listOf().parse(JsonOps.INSTANCE, data).getOrThrow(false, s -> {});
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case SMART, EXTEND -> {
                JsonElement element = old.deepCopy();
                element.getAsJsonArray().addAll(toMerge.getAsJsonArray());
                return element;
            }
            case OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    public List<StatusEffectData> get(ItemStack itemStack) {
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element == null) {
            return null;
        }
        return StatusEffectData.CODEC(ItemModule.getModules(itemStack)).listOf().parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {});
    }

    public static List<StatusEffectData> createCache(ItemStack stack) {
        ItemModule.ModuleInstance root = ItemModule.getModules(stack);
        List<StatusEffectData> data = new ArrayList<>();
        for (ItemModule.ModuleInstance module : root.allSubModules()) {
            setupModuleEffects(module, data);
        }
        return data;
    }

    public static void setupModuleEffects(ItemModule.ModuleInstance module, List<StatusEffectData> list) {
        JsonElement element = module.getProperties().get(property);
        if (element == null) return;

        list.addAll(StatusEffectData.CODEC(module).listOf().parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {}));
    }

    public boolean hasPotionEffectData(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    public record StatusEffectData(ApplicationEvent event, ApplicationTarget target, Supplier<StatusEffectInstance> creator, StatusEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon) {
        public static Codec<StatusEffectData> CODEC(ItemModule.ModuleInstance instance) {
            return RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("event").forGetter(i -> i.event.name()),
                    Codec.STRING.fieldOf("target").forGetter(i -> i.target.name()),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("duration").forGetter(i -> i.duration),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("amplifier").forGetter(i -> i.amplifier),
                    Registry.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(i -> i.effect), // 1.19.3+ this turns into BuiltinRegistries.STATUS_EFFECT...
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter(i -> i.ambient),
                    Codec.BOOL.optionalFieldOf("visible", true).forGetter(i -> i.visible),
                    Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(i -> i.showIcon)
            ).apply(inst, StatusEffectData::create));
        }

        public static StatusEffectData create(String applyEvent, String applyTarget, int duration, int amplifier, StatusEffect effect, boolean ambient, boolean visible, boolean showIcon) {
            return new StatusEffectData(
                    ApplicationEvent.fromName(applyEvent),
                    ApplicationTarget.fromName(applyTarget),
                    () -> new StatusEffectInstance(effect, duration, amplifier, ambient, visible, showIcon),
                    effect,
                    duration,
                    amplifier,
                    ambient,
                    visible,
                    showIcon
            );
        }
    }

    /**
     * {@link ApplicationEvent} is an enum representing when the {@link StatusEffectInstance} will be applied.
     */
    public enum ApplicationEvent {
        HURT,
        ATTACK,
        ABILITY_START,
        ABILITY_TICK,
        ABILITY_STOP,
        ABILITY_FINISH;

        public ApplicationEvent inverse() {
            switch (this) {
                case HURT -> { return ATTACK; }
                case ATTACK -> { return HURT; }
            }
            return this;
        }

        public static ApplicationEvent fromName(String name) {
            try {
                return ApplicationEvent.valueOf(name.toUpperCase());
            } catch (Exception e) {
                Miapi.LOGGER.error("Failed to create ApplicationEvent from {}. Please using correct spelling: [ hurt, attack ]", name);
                e.printStackTrace();
            }
            return ApplicationEvent.HURT;
        }
    }
    /**
     * {@link ApplicationTarget} is an enum representing what entity the {@link StatusEffectInstance} will be applied to.
     */
    public enum ApplicationTarget {
        SELF,
        TARGET;

        public ApplicationTarget inverse() {
            switch (this) {
                case SELF -> { return TARGET; }
                case TARGET -> { return SELF; }
            }
            return this;
        }

        public static ApplicationTarget fromName(String name) {
            try {
                return ApplicationTarget.valueOf(name.toUpperCase());
            } catch (Exception e) {
                Miapi.LOGGER.error("Failed to create ApplicationTarget from '{}'. Please using correct spelling: [ self, target ]", name);
                e.printStackTrace();
            }
            return ApplicationTarget.SELF;
        }
    }
}
