package smartin.miapi.modules.properties.onHit;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The `NemesisProperty` class provides a property for items that deals bonus damage to specific target entities based on the number of kills made.
 *
 * @header Nemesis Property
 * @path /data_types/properties/onHit/nemesis
 * @description_start
 * The Nemesis Property allows weapons to deal increased damage to certain types of entities. As the weapon is used to kill more of the specified entity type, it gains additional damage potential against them. If the weapon is used against other types of entities, it loses some of its bonus damage.
 * This property tracks the number of kills made and adjusts the damage dealt based on the entity type and the number of kills recorded. It also updates the item's tooltip to reflect the current state of its nemesis status.
 * @description_end
 * @data value:the scaling, how high and fast it gains damage
 */

public class NemesisProperty extends DoubleProperty implements CraftingProperty {
    public static final ResourceLocation KEY = Miapi.id("nemesis");
    public static NemesisProperty property;
    public static Codec<NemesisData> CODEC = AutoCodec.of(NemesisData.class).codec();
    public static DataComponentType<NemesisData> NEMESIS_COMPONENT = DataComponentType.<NemesisData>builder().persistent(CODEC).networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();


    public DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.#"), (decimalFormat) -> {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public NemesisProperty() {
        super(KEY);
        setupLore();
        property = this;
        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            ItemStack weapon = MiapiEvents.LivingHurtEvent.getCausingItemStack(damageSource);
            if (weapon.getItem() instanceof ModularItem && !livingEntity.level().isClientSide()) {
                double nemesisScale = getValue(weapon).orElse(0.0);
                NemesisData data = weapon.get(NEMESIS_COMPONENT);
                if (data != null && nemesisScale > 0) {
                    EntityType attackedType = livingEntity.getType();
                    Optional<EntityType<?>> entityType1 = EntityType.byString(data.entityType);
                    if (entityType1.isPresent()) {
                        EntityType targetType = entityType1.get();
                        if (attackedType.equals(targetType)) {
                            data.kills += 1;
                        } else {
                            //other type
                            data.kills -= 5;
                            if (data.kills < 0) {
                                data.kills = 0;
                            }
                            weapon.set(NEMESIS_COMPONENT, data);
                        }
                    } else {
                        data.entityType = EntityType.getKey(attackedType).toString();
                        data.kills = 1;
                    }
                }
                weapon.set(NEMESIS_COMPONENT, data);
            }
            return EventResult.pass();
        });
        MiapiEvents.LIVING_HURT.register((listener) -> {
            ItemStack weapon = listener.getCausingItemStack();
            if (weapon.getItem() instanceof ModularItem) {
                double nemesisScale = getValue(weapon).orElse(0.0);
                NemesisData data = weapon.get(NEMESIS_COMPONENT);
                if (data != null && nemesisScale > 0) {
                    EntityType attackedType = listener.livingEntity.getType();

                    Optional<EntityType<?>> entityType1 = EntityType.byString(data.entityType);

                    if (entityType1.isPresent()) {
                        EntityType targetType = entityType1.get();
                        if (attackedType.equals(targetType)) {
                            double factor = scale(data.kills, nemesisScale);
                            listener.amount += (float) (factor) * listener.amount;
                        } else {
                            double factor = scale(data.kills, nemesisScale);
                            factor = Math.min(0.95, factor);
                            listener.amount -= (float) (factor) * listener.amount;
                        }
                    }
                    weapon.set(NEMESIS_COMPONENT, data);
                }
            }
            return EventResult.pass();
        });
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((ItemStack weapon, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            double nemesisScale = getValue(weapon).orElse(0.0);
            NemesisData data = weapon.get(NEMESIS_COMPONENT);
            if (data != null && nemesisScale > 0) {
                double factor = scale(data.kills, nemesisScale) * 100 - 1;
                Optional<EntityType<?>> entityType1 = EntityType.byString(data.entityType);
                Component entity = Component.translatable("miapi.lore.nemesis.no_entity");
                if (entityType1.isPresent()) {
                    entity = entityType1.get().getDescription();
                }
                Component blueNumber = Component.literal(modifierFormat.format(factor) + "%").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE));
                Component redNumber = Component.literal(modifierFormat.format(factor / 2) + "%").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                Component whiteNumber = Component.literal(String.valueOf(data.kills)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
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
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        crafting.set(NEMESIS_COMPONENT, new NemesisData("", 0));
        return crafting;
    }

    public static class NemesisData {
        @AutoCodec.Name("entity_type")
        public String entityType;
        public int kills;

        public NemesisData(String entityType, int kills) {
            this.entityType = entityType;
            this.kills = kills;
        }
    }
}
