package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import smartin.miapi.events.Event;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.PropertyApplication;
import smartin.miapi.modules.properties.util.SimpleEventProperty;

import java.util.*;
import java.util.function.Supplier;

import static smartin.miapi.modules.properties.util.PropertyApplication.ApplicationEvent.*;

public class PotionEffectProperty extends SimpleEventProperty {

    public static String KEY = "applyPotionEffects";
    public static PotionEffectProperty property;

    public PotionEffectProperty() {
        super(
                new EventHandlingMap<>()
                        .set(HURT, PotionEffectProperty::onEntityHurt)
                        .setAll(ABILITIES, PotionEffectProperty::onAbility)
        );
        property = this;
        ModularItemCache.setSupplier(KEY, PotionEffectProperty::createCache);
    }

    public static void onEntityHurt(Event.LivingHurtEvent event) {
        LivingEntity victim = event.livingEntity;
        if (!(victim.world instanceof ServerWorld world) || !(event.damageSource.getAttacker() instanceof LivingEntity attacker)) return;
        LootConditionManager predicateManager = victim.getServer() == null ? null : victim.getServer().getPredicateManager();

        List<StatusEffectData> effects = ofEntity(victim); // setting up and merging effect data
        effects.addAll(ofEntity(attacker).stream()
                .map(d -> new StatusEffectData(d.event, d.targetInverse(), d.creator, d.effect, d.duration, d.amplifier, d.ambient, d.visible, d.showIcon, d.predicate, !d.shouldReverse)) // inverting
                .toList()
        );

        for (StatusEffectData effect : effects) {
            if (!effect.event.equals(HURT) || effect.shouldReverse) continue;
            LivingEntity toApply = victim;
            if (effect.target.equals("target"))
                toApply = attacker;

            if (predicateManager == null || effect.predicate.isEmpty())
                toApply.addStatusEffect(effect.creator.get());
            else {
                LootCondition condition = predicateManager.get(effect.predicate.get());
                if (condition != null) {
                    LootContext.Builder builder = new LootContext.Builder(world)
                            .parameter(LootContextParameters.THIS_ENTITY, toApply) // THIS_ENTITY is whomever the effect is applied to
                            .parameter(LootContextParameters.ORIGIN, toApply.getPos())
                            .parameter(LootContextParameters.DAMAGE_SOURCE, event.damageSource)
                            .parameter(LootContextParameters.KILLER_ENTITY, event.damageSource.getAttacker())
                            .parameter(LootContextParameters.DIRECT_KILLER_ENTITY, event.damageSource.getSource());
                    if (toApply.getAttacker() instanceof PlayerEntity player)
                        builder.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, player);

                    if (condition.test(builder.build(LootContextTypes.ENTITY)))
                        toApply.addStatusEffect(effect.creator.get());
                } else
                    Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
            }
        }
    }
    public static void onAbility(PropertyApplication.ApplicationEvent<PropertyApplication.Holders.Ability> event, PropertyApplication.Holders.Ability ability) {
        List<PotionEffectProperty.StatusEffectData> potionEffects = property.get(ability.stack());
        if (potionEffects != null) {
            for (StatusEffectData effect : potionEffects) {
                if (!effect.event.equals(event)) continue;

                ability.user().addStatusEffect(effect.creator.get());
            }
        }
    }

    public static List<StatusEffectData> ofEntity(LivingEntity entity) {
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

    public static final class StatusEffectData {
        public static Codec<StatusEffectData> CODEC(ItemModule.ModuleInstance instance) {
            return RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("event").forGetter(i -> i.event.name),
                    Codec.STRING.fieldOf("target").forGetter(i -> i.target),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("duration").forGetter(i -> i.duration),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("amplifier").forGetter(i -> i.amplifier),
                    Registry.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(i -> i.effect), // 1.19.3+ this turns into BuiltinRegistries.STATUS_EFFECT...
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter(i -> i.ambient),
                    Codec.BOOL.optionalFieldOf("visible", true).forGetter(i -> i.visible),
                    Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(i -> i.showIcon),
                    Identifier.CODEC.optionalFieldOf("predicate").forGetter(i -> i.predicate) // allow loot table predicates. ENTITY context is provided for living hurt event (see LootContextTypes.ENTITY)
            ).apply(inst, StatusEffectData::create));
        }

        private final PropertyApplication.ApplicationEvent<?> event;
        private String target;
        private final Supplier<StatusEffectInstance> creator;
        private final StatusEffect effect;
        private final int duration;
        private final int amplifier;
        private final boolean ambient;
        private final boolean visible;
        private final boolean showIcon;
        private final Optional<Identifier> predicate;
        private boolean shouldReverse;

        public StatusEffectData(PropertyApplication.ApplicationEvent<?> event, String target, Supplier<StatusEffectInstance> creator, StatusEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, Optional<Identifier> predicate) {
            this(event, target, creator, effect, duration, amplifier, ambient, visible, showIcon, predicate, false);
        }
        public StatusEffectData(PropertyApplication.ApplicationEvent<?> event, String target, Supplier<StatusEffectInstance> creator, StatusEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon, Optional<Identifier> predicate, boolean shouldReverse) {
            this.event = event;
            this.target = target;
            this.creator = creator;
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.visible = visible;
            this.showIcon = showIcon;
            this.predicate = predicate;
            this.shouldReverse = shouldReverse;
        }

        public static StatusEffectData create(String applyEvent, String applyTarget, int duration, int amplifier, StatusEffect effect, boolean ambient, boolean visible, boolean showIcon, Optional<Identifier> predicateLocation) {
            StatusEffectData data = new StatusEffectData(
                    PropertyApplication.ApplicationEvent.get(applyEvent),
                    applyTarget,
                    () -> new StatusEffectInstance(effect, duration, amplifier, ambient, visible, showIcon),
                    effect,
                    duration,
                    amplifier,
                    ambient,
                    visible,
                    showIcon,
                    predicateLocation
            );

            if (applyEvent.equalsIgnoreCase("attack"))
                data.shouldReverse = true;
            return data;
        }

        public String targetInverse() {
            switch (target) {
                case "this" -> { return "target"; }
                case "target" -> { return "this"; }
            }
            return "this";
        }

        public PropertyApplication.ApplicationEvent<?> event() {
            return event;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (StatusEffectData) obj;
            return Objects.equals(this.event, that.event) &&
                    Objects.equals(this.target, that.target) &&
                    Objects.equals(this.creator, that.creator) &&
                    Objects.equals(this.effect, that.effect) &&
                    this.duration == that.duration &&
                    this.amplifier == that.amplifier &&
                    this.ambient == that.ambient &&
                    this.visible == that.visible &&
                    this.showIcon == that.showIcon &&
                    Objects.equals(this.predicate, that.predicate) &&
                    this.shouldReverse == that.shouldReverse;
        }

        @Override
        public int hashCode() {
            return Objects.hash(event, target, creator, effect, duration, amplifier, ambient, visible, showIcon, predicate, shouldReverse);
        }

        @Override
        public String toString() {
            return "StatusEffectData[" +
                    "event=" + event + ", " +
                    "target=" + target + ", " +
                    "creator=" + creator + ", " +
                    "effect=" + effect + ", " +
                    "duration=" + duration + ", " +
                    "amplifier=" + amplifier + ", " +
                    "ambient=" + ambient + ", " +
                    "visible=" + visible + ", " +
                    "showIcon=" + showIcon + ", " +
                    "predicate=" + predicate + ", " +
                    "shouldReverse=" + shouldReverse + ']';
        }
    }
}
