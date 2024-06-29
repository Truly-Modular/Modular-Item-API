package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Nemesis is a Complicated Property to deal bonus damage against certain targets
 */
public class NemesisProperty extends DoubleProperty implements CraftingProperty {
    public static String KEY = "nemesis";
    public static NemesisProperty property;

    public DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.#"), (decimalFormat) -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    //TODO:rework into Component
    public NemesisProperty() {
        super(KEY);
        setupLore();
        property = this;
        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            ItemStack weapon = MiapiEvents.LivingHurtEvent.getCausingItemStack(damageSource);
            if (weapon.getItem() instanceof ModularItem && !livingEntity.level().isClientSide()) {
                Double nemesisScale = getValue(weapon);
                CompoundTag compound = weapon.getOrCreateNbt();
                if (nemesisScale != null && nemesisScale > 0) {
                    String entityType = compound.getString("miapi_nemesis_target");
                    int value = compound.getInt("miapi_nemesis");
                    EntityType attackedType = livingEntity.getType();
                    Optional<EntityType<?>> entityType1 = EntityType.byString(entityType);
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
                        compound.putString("miapi_nemesis_target", EntityType.getKey(attackedType).toString());
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
                CompoundTag compound = weapon.getOrCreateNbt();
                if (nemesisScale != null && nemesisScale > 0) {
                    String entityType = compound.getString("miapi_nemesis_target");
                    int value = compound.getInt("miapi_nemesis");

                    EntityType attackedType = listener.livingEntity.getType();

                    Optional<EntityType<?>> entityType1 = EntityType.byString(entityType);

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

    public void setupLore() {
        LoreProperty.loreSuppliers.add((ItemStack weapon, @Nullable Level world, List<Component> tooltip, TooltipContext context) -> {
            Double nemesisScale = getValue(weapon);
            CompoundTag compound = weapon.getOrCreateNbt();
            if (nemesisScale != null && nemesisScale > 0) {
                String entityType = compound.getString("miapi_nemesis_target");
                int value = compound.getInt("miapi_nemesis");
                double factor = scale(value, nemesisScale) * 100 - 1;
                Optional<EntityType<?>> entityType1 = EntityType.byString(entityType);
                Component entity = Component.translatable("miapi.lore.nemesis.no_entity");
                if (entityType1.isPresent()) {
                    entity = entityType1.get().getDescription();
                }
                Component blueNumber = Component.literal(modifierFormat.format(factor) + "%").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE));
                Component redNumber = Component.literal(modifierFormat.format(factor/2) + "%").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                Component whiteNumber = Component.literal(String.valueOf(value)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
                entity = Component.literal(entity.getString()).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("miapi.lore.nemesis.0", whiteNumber, entity));
                if (factor != 0) {
                    tooltip.add(Component.translatable("miapi.lore.nemesis.1", blueNumber, Component.literal(entity.getString()).withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE))));
                    tooltip.add(Component.translatable("miapi.lore.nemesis.2", redNumber, Component.literal(entity.getString()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            }
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        crafting.removeSubNbt("miapi_nemesis");
        return crafting;
    }
}
