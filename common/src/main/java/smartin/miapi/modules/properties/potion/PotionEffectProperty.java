package smartin.miapi.modules.properties.potion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract class to create properties that use Potion effects
 */
public abstract class PotionEffectProperty implements ModuleProperty {
    public String KEY;
    public PotionEffectProperty property;

    protected PotionEffectProperty(String key) {
        KEY = key;
        property = this;
        ModularItemCache.setSupplier(KEY + ".status_effects", this::getStatusEffectsCache);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        getPotions(data, new ModuleInstance(ItemModule.empty));
        return true;
    }

    public void applyPotions(LivingEntity livingEntity, Iterable<ItemStack> itemStack, @Nullable LivingEntity causer) {
        List<EffectHolder> effectHolders = getStatusEffects(itemStack);
        for (EffectHolder effectHolder : effectHolders) {
            livingEntity.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public List<EffectHolder> getStatusEffects(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY + ".status_effects", new ArrayList<>());
    }

    public List<EffectHolder> getStatusEffects(ItemStack itemStack, EffectPredicate predicate, LivingEntity livingEntity) {
        List<EffectHolder> holders = new ArrayList<>();
        getStatusEffects(itemStack)
                .stream()
                .filter(effectHolder -> isValidForSlot(EquipmentSlot.MAINHAND, effectHolder, livingEntity))
                .filter(effectHolder -> predicate.filterHolder(effectHolder, EquipmentSlot.MAINHAND))
                .forEach(holders::add);
        return holders;
    }


    public void applyEffects(LivingEntity target, LivingEntity itemsFromEntity, @Nullable LivingEntity causer) {
        List<EffectHolder> getFilteredEffects = getHoldersConditional(itemsFromEntity);
        getFilteredEffects = merge(getFilteredEffects);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public void applyEffects(LivingEntity target, LivingEntity itemsFromEntity, @Nullable LivingEntity causer, EffectPredicate predicate) {
        List<EffectHolder> getFilteredEffects = getHoldersConditional(itemsFromEntity, predicate);
        getFilteredEffects = merge(getFilteredEffects);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public void applyEffects(LivingEntity target, ItemStack itemStack, @Nullable LivingEntity causer, EffectPredicate predicate) {
        List<EffectHolder> getFilteredEffects = getStatusEffects(itemStack);
        getFilteredEffects = merge(getFilteredEffects);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    /**
     * uses Armor Items and supplied Attacking Itemstack. use this in most cases
     *
     * @param target
     * @param itemStack
     * @param causer
     * @param predicate
     */
    public void applyEffects(LivingEntity target, LivingEntity itemsFromEntity, ItemStack itemStack, @Nullable LivingEntity causer, EffectPredicate predicate) {
        List<EffectHolder> getFilteredEffects = getStatusEffects(itemStack, predicate, itemsFromEntity);
        getFilteredEffects = merge(getFilteredEffects);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
        getFilteredEffects = getHoldersConditional(itemsFromEntity, predicate);
        getFilteredEffects = merge(getFilteredEffects);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public List<EffectHolder> merge(List<EffectHolder> holders) {
        List<EffectHolder> filtered = new ArrayList<>();
        Map<EffectKey, EffectHolder> effectMap = new HashMap<>();

        for (EffectHolder holder : holders) {
            int amplifier = holder.effectInstance().getAmplifier();
            StatusEffect effect = holder.statusEffect();
            EffectKey key = new EffectKey(effect, amplifier);

            if (effectMap.containsKey(key)) {
                // Invoke merging logic
                EffectHolder old = effectMap.get(key);
                filtered.remove(old);
                EffectHolder merged = mergeEffects(holder, old);
                effectMap.put(key, merged);
                filtered.add(merged);
            } else {
                effectMap.put(key, holder);
                filtered.add(holder);
            }
        }
        return filtered;
    }

    private EffectHolder mergeEffects(EffectHolder toMerge, EffectHolder existing) {
        StatusEffectInstance oldInstance = existing.effectInstance();
        StatusEffectInstance toMergeInstance = toMerge.effectInstance();

        StatusEffectInstance instance = new StatusEffectInstance(
                oldInstance.getEffectType(),
                oldInstance.getDuration() + toMergeInstance.getDuration(),
                oldInstance.getAmplifier(),
                oldInstance.isAmbient() || toMergeInstance.isAmbient(),
                oldInstance.shouldShowParticles() || toMergeInstance.shouldShowParticles());
        return new EffectHolder(existing.statusEffect, existing.moduleInstance, existing.rawData, instance);
    }

    private static class EffectKey {
        private StatusEffect effect;
        private int amplifier;

        public EffectKey(StatusEffect effect, int amplifier) {
            this.effect = effect;
            this.amplifier = amplifier;
        }

        @Override
        public int hashCode() {
            return effect.hashCode() + amplifier;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            EffectKey other = (EffectKey) obj;
            return effect.equals(other.effect) && amplifier == other.amplifier;
        }
    }

    public List<EffectHolder> getHoldersConditional(LivingEntity entity, EffectPredicate predicate) {
        List<EffectHolder> effectHolders = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getEquippedStack(slot);
            getStatusEffects(itemStack)
                    .stream()
                    .filter(effectHolder -> isValidForSlot(slot, effectHolder, entity))
                    .filter(effectHolder -> predicate.filterHolder(effectHolder, slot))
                    .forEach(effectHolders::add);
        }
        return effectHolders;
    }

    public List<EffectHolder> getHoldersConditional(LivingEntity entity) {
        List<EffectHolder> effectHolders = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getEquippedStack(slot);
            getStatusEffects(itemStack)
                    .stream()
                    .filter(effectHolder -> isValidForSlot(slot, effectHolder, entity))
                    .forEach(effectHolders::add);
        }
        return effectHolders;
    }

    public boolean isValidForSlot(EquipmentSlot equipmentSlot, EffectHolder holder, LivingEntity livingEntity) {
        String key = ModuleProperty.getString(holder.rawData(), "equipment_slot", holder.moduleInstance(), "all");
        switch (key) {
            case "all" -> {
                return true;
            }
            case "armor" -> {
                return equipmentSlot.isArmorSlot();
            }
            case "hand" -> {
                return !equipmentSlot.isArmorSlot();
            }
            case "active_hand" -> {
                switch (livingEntity.getActiveHand()) {
                    case OFF_HAND -> {
                        return EquipmentSlot.MAINHAND.equals(equipmentSlot);
                    }
                    case MAIN_HAND -> {
                        return EquipmentSlot.OFFHAND.equals(equipmentSlot);
                    }
                }
                return false;
            }
            default -> {
                return equipmentSlot.equals(EquipmentSlot.byName(key));
            }
        }
    }

    public List<EffectHolder> getStatusEffects(Iterable<ItemStack> items) {
        List<EffectHolder> potions = new ArrayList<>();
        for (ItemStack itemStack : items) {
            potions.addAll(getStatusEffectsCache(itemStack));
        }
        return potions;
    }

    private List<EffectHolder> getStatusEffectsCache(ItemStack itemStack) {
        List<EffectHolder> potions = new ArrayList<>();
        ItemModule.getModules(itemStack).allSubModules().forEach(moduleInstance -> {
            if (moduleInstance.getProperties().containsKey(this)) {
                potions.addAll(getPotions(moduleInstance.getProperties().get(this), moduleInstance));
            }
        });
        return potions;
    }

    public List<EffectHolder> getPotions(JsonElement jsonElement, ModuleInstance moduleInstance) {
        List<EffectHolder> potions = new ArrayList<>();
        jsonElement.getAsJsonArray().forEach(element -> {
            JsonObject object = element.getAsJsonObject();
            Identifier identifier = new Identifier(ModuleProperty.getString(object, "potion", moduleInstance, ""));
            StatusEffect potion = Registries.STATUS_EFFECT.get(identifier);
            if (potion != null) {
                potions.add(getHolder(potion, element.getAsJsonObject(), moduleInstance));
            } else {
                Miapi.LOGGER.warn("could not find Potion " + identifier);
            }
        });
        return potions;
    }

    public EffectHolder getHolder(StatusEffect effect, JsonObject object, ModuleInstance moduleInstance) {
        return new EffectHolder(effect, moduleInstance, object, null);
    }

    public record EffectHolder(StatusEffect statusEffect, ModuleInstance moduleInstance,
                               JsonObject rawData, StatusEffectInstance instance) {

        public boolean isGuiVisibility() {
            return ModuleProperty.getBoolean(rawData(), "lore", moduleInstance(), true);
        }

        public StatusEffectInstance effectInstance() {
            if (instance != null) {
                return instance;
            }
            int duration = ModuleProperty.getInteger(rawData(), "duration", moduleInstance(), 10);
            int amplifier = ModuleProperty.getInteger(rawData(), "amplifier", moduleInstance(), 0);
            boolean ambient = ModuleProperty.getBoolean(rawData(), "ambient", moduleInstance(), false);
            boolean showParticles = ModuleProperty.getBoolean(rawData(), "showParticles", moduleInstance(), true);
            boolean showIcon = ModuleProperty.getBoolean(rawData(), "showIcon", moduleInstance(), showParticles);
            return new StatusEffectInstance(statusEffect(), duration, amplifier, ambient, showParticles, showIcon);
        }

        public Text getPotionDescription() {
            Text text = Text.translatable(effectInstance().getTranslationKey());
            text = text.getWithStyle(Style.EMPTY.withColor(effectInstance().getEffectType().getColor())).get(0);
            return text;
        }

        public int getDurationSeconds() {
            return (int) Math.ceil((float) effectInstance().getDuration() / 20.0f);
        }

        public Text getAmplifier() {
            return Text.translatable("potion.potency." + effectInstance().getAmplifier());
        }
    }

    public interface EffectPredicate {
        boolean filterHolder(EffectHolder effectHolder, EquipmentSlot equipmentSlot);
    }
}
