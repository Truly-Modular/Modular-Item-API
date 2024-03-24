package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Nemesis is a Complicated Property to deal bonus damage against certain targets
 */
public class NemesisProperty extends DoubleProperty implements CraftingProperty {
    public static String KEY = "nemesis";
    public static NemesisProperty property;

    public DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.#"), (decimalFormat) -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public NemesisProperty() {
        super(KEY);
        if (smartin.miapi.Environment.isClient()) {
            setupClient();
        }
        property = this;
        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            ItemStack weapon = MiapiEvents.LivingHurtEvent.getCausingItemStack(damageSource);
            if (weapon.getItem() instanceof ModularItem && !livingEntity.getWorld().isClient()) {
                Double nemesisScale = getValue(weapon);
                NbtCompound compound = weapon.getOrCreateNbt();
                if (nemesisScale != null && nemesisScale > 0) {
                    String entityType = compound.getString("miapi_nemesis_target");
                    int value = compound.getInt("miapi_nemesis");
                    EntityType attackedType = livingEntity.getType();
                    Optional<EntityType<?>> entityType1 = EntityType.get(entityType);
                    if (entityType1.isPresent()) {
                        EntityType targetType = entityType1.get();
                        if (attackedType.equals(targetType)) {
                            compound.putInt("miapi_nemesis", value + 1);
                        } else {
                            //other type
                            value = value - 5;
                            if (value < 0) {
                                compound.remove("miapi_nemesis_target");
                                compound.putInt("miapi_nemesis", 0);
                            } else {
                                compound.putInt("miapi_nemesis", value);
                            }
                        }
                    } else {
                        compound.putString("miapi_nemesis_target", EntityType.getId(attackedType).toString());
                        compound.putInt("miapi_nemesis", 1);
                    }
                }
                weapon.setNbt(compound);
            }
            return EventResult.pass();
        });
        MiapiEvents.LIVING_HURT.register((listener) -> {
            ItemStack weapon = listener.getCausingItemStack();
            if (weapon.getItem() instanceof ModularItem) {
                Double nemesisScale = getValue(weapon);
                NbtCompound compound = weapon.getOrCreateNbt();
                if (nemesisScale != null && nemesisScale > 0) {
                    String entityType = compound.getString("miapi_nemesis_target");
                    int value = compound.getInt("miapi_nemesis");

                    EntityType attackedType = listener.livingEntity.getType();

                    Optional<EntityType<?>> entityType1 = EntityType.get(entityType);

                    if (entityType1.isPresent()) {
                        EntityType targetType = entityType1.get();
                        if (attackedType.equals(targetType)) {
                            double factor = scale(value, nemesisScale);
                            listener.amount += (float) (factor) * listener.amount;
                        } else {
                            double factor = scale(value, nemesisScale);
                            factor = Math.min(0.95, factor);
                            listener.amount -= (float) (factor) * listener.amount;
                        }
                    }
                }
                weapon.setNbt(compound);
            }
            return EventResult.pass();
        });
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        LoreProperty.loreSuppliers.add(weapon -> {
            List<Text> lore = new ArrayList<>();
            Double nemesisScale = getValue(weapon);
            NbtCompound compound = weapon.getOrCreateNbt();
            if (nemesisScale != null && nemesisScale > 0) {
                String entityType = compound.getString("miapi_nemesis_target");
                int value = compound.getInt("miapi_nemesis");
                double factor = scale(value, nemesisScale) * 100 - 1;
                Optional<EntityType<?>> entityType1 = EntityType.get(entityType);
                Text entity = Text.translatable("miapi.lore.nemesis.no_entity");
                if (entityType1.isPresent()) {
                    entity = entityType1.get().getName();
                }
                Text blueNumber = Text.literal(modifierFormat.format(factor) + "%").fillStyle(Style.EMPTY.withColor(Formatting.BLUE));
                Text redNumber = Text.literal(modifierFormat.format(factor) + "%").fillStyle(Style.EMPTY.withColor(Formatting.RED));
                Text whiteNumber = Text.literal(String.valueOf(value)).fillStyle(Style.EMPTY.withColor(Formatting.WHITE));
                entity = Text.literal(entity.getString()).fillStyle(Style.EMPTY.withColor(Formatting.GRAY));
                lore.add(Text.translatable("miapi.lore.nemesis.0", whiteNumber, entity));
                if (factor != 0) {
                    lore.add(Text.translatable("miapi.lore.nemesis.1", blueNumber, Text.literal(entity.getString()).fillStyle(Style.EMPTY.withColor(Formatting.BLUE))));
                    lore.add(Text.translatable("miapi.lore.nemesis.2", redNumber, Text.literal(entity.getString()).fillStyle(Style.EMPTY.withColor(Formatting.RED))));
                }
            }
            return lore;
        });
    }

    public static float scale(int rawValue, double scale) {
        double factor = ((Math.log(Math.pow((rawValue + 1), 5)) + 1) * scale);
        if (rawValue < 0) {
            factor = 0;
        }
        return (float) factor / 100;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        crafting.removeSubNbt("miapi_nemesis");
        return crafting;
    }
}
