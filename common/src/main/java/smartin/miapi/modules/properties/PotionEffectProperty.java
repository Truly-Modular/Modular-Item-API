package smartin.miapi.modules.properties;

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
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class to create properties that use Potion effects
 */
public abstract class PotionEffectProperty implements ModuleProperty {
    public String KEY;
    public PotionEffectProperty property;

    protected PotionEffectProperty(String key, Text text) {
        KEY = key;
        property = this;
        ModularItemCache.setSupplier(KEY + ".status_effects", this::getStatusEffectsCache);
        LoreProperty.loreSuppliers.add(itemStack -> {
            List<Text> lines = new ArrayList<>();
            for (EffectHolder effectHolder : getStatusEffects(itemStack)) {
                if (effectHolder.isGuiVisibility()) {
                    lines.add(effectHolder.getPotionDescription());
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, text);
            }
            return lines;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement left, JsonElement right, MergeType mergeType) {
        return ModuleProperty.mergeAsMap(left, right, mergeType);
    }

    public void applyPotions(LivingEntity livingEntity, Iterable<ItemStack> itemStack) {
        applyPotions(livingEntity, itemStack, null);
    }

    public void applyPotions(LivingEntity livingEntity, Iterable<ItemStack> itemStack, @Nullable LivingEntity causer) {
        List<EffectHolder> effectHolders = getStatusEffects(itemStack);
        for (EffectHolder effectHolder : effectHolders) {
            livingEntity.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public void applyEffects(LivingEntity target, LivingEntity itemsFromEntity, @Nullable LivingEntity causer) {
        List<EffectHolder> getFilteredEffects = getHoldersConditional(itemsFromEntity);
        for (EffectHolder effectHolder : getFilteredEffects) {
            target.addStatusEffect(effectHolder.effectInstance(), causer);
        }
    }

    public List<EffectHolder> getStatusEffects(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY + ".status_effects", new ArrayList<>());
    }

    public List<EffectHolder> getHoldersConditional(LivingEntity entity) {
        List<EffectHolder> effectHolders = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = entity.getEquippedStack(slot);
            List<EffectHolder> holders = getStatusEffects(itemStack);
            holders.stream().filter(effectHolder -> effectHolder.validEquipmentSlot(slot)).forEach(effectHolders::add);
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

    public List<EffectHolder> getPotions(JsonElement jsonElement, ItemModule.ModuleInstance moduleInstance) {
        List<EffectHolder> potions = new ArrayList<>();
        jsonElement.getAsJsonObject().asMap().forEach((id, element) -> {
            StatusEffect potion = Registries.STATUS_EFFECT.get(new Identifier(id));
            if (potion != null) {
                potions.add(getHolder(potion, element.getAsJsonObject(), moduleInstance));
            } else {
                Miapi.LOGGER.warn("could not find Potion " + id);
            }
        });
        return potions;
    }

    public EffectHolder getHolder(StatusEffect effect, JsonObject object, ItemModule.ModuleInstance moduleInstance) {
        int duration = ModuleProperty.getInteger(object, "duration", moduleInstance, 10);
        int amplifier = ModuleProperty.getInteger(object, "amplifier", moduleInstance, 0);
        return new EffectHolder(new StatusEffectInstance(effect, duration, amplifier), moduleInstance, object);
    }

    public record EffectHolder(StatusEffectInstance effectInstance, ItemModule.ModuleInstance moduleInstance,
                               JsonObject rawData) {
        public boolean isGuiVisibility() {
            return ModuleProperty.getBoolean(rawData(), "gui_visible", moduleInstance(), true);
        }

        public Text getPotionDescription() {
            Text text = Text.translatable(("effect." + Registries.STATUS_EFFECT.getId(effectInstance().getEffectType())).replace(":", "."));
            text = text.getWithStyle(Style.EMPTY.withColor(effectInstance().getEffectType().getColor())).get(0);
            return text;
        }

        public boolean validEquipmentSlot(EquipmentSlot equipmentSlot) {
            return true;
        }
    }
}
