package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * @header Handheld Item Property
 * @path /data_types/properties/handheld_item
 * @description_start
 * The HandheldItemProperty provides functionality for items that grant bonuses or perform special actions when held in
 * both the main hand and off hand. Specifically, this property gives +50% attack speed if both hands contain items with
 * this property. Additionally, it can automatically swap items between hands when better combat is not present.
 * @description_end
 * @data handheld_item: a boolean value if this is a handheld item. default is false
 */

public class HandheldItemProperty extends ComplexBooleanProperty {
    public static ResourceLocation attackspeedID = Miapi.id("handheld_bonus_attackspeed");
    public static final ResourceLocation KEY = Miapi.id("handheld_item");

    public HandheldItemProperty() {
        super(KEY, false);
        MiapiEvents.PLAYER_EQUIP_EVENT.register((player, changes) -> {
            if (player instanceof ServerPlayer serverPlayerEntity) {
                ItemStack mainHandItem = changes.getOrDefault(EquipmentSlot.MAINHAND, player.getItemBySlot(EquipmentSlot.MAINHAND));
                ItemStack offHandItem = changes.getOrDefault(EquipmentSlot.OFFHAND, player.getItemBySlot(EquipmentSlot.OFFHAND));
                boolean hasAttribute = serverPlayerEntity
                                               .getAttributes()
                                               .getInstance(Attributes.ATTACK_SPEED).getModifier(attackspeedID) != null;
                if (hasTwoHandhelds(mainHandItem, offHandItem)) {
                    if(!hasAttribute){
                        serverPlayerEntity
                                .getAttributes()
                                .getInstance(Attributes.ATTACK_SPEED)
                                .addTransientModifier(
                                        new AttributeModifier(
                                                attackspeedID,
                                                1.5,
                                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    }

                } else {
                    if(hasAttribute){
                        serverPlayerEntity
                                .getAttributes()
                                .getInstance(Attributes.ATTACK_SPEED)
                                .removeModifier(attackspeedID);
                    }
                }
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            if (event.damageSource != null && event.damageSource.getEntity() instanceof Player serverPlayerEntity) {
                if (hasTwoHandhelds(serverPlayerEntity) && !Platform.isModLoaded("bettercombat")) {
                    swapHands(serverPlayerEntity);
                }
            }
            return EventResult.pass();
        });
        LoreProperty.loreSuppliers.add((stack, tooltip, context, flag) -> {
            if (hasValue(stack)) {
                tooltip.add(Component.translatable(Miapi.MOD_ID + ".handheld.tooltip"));
            }
        });
    }

    public void swapHands(Player playerEntity) {
        ItemStack itemStack = playerEntity.getItemBySlot(EquipmentSlot.OFFHAND);
        playerEntity.setItemSlot(EquipmentSlot.OFFHAND, playerEntity.getItemBySlot(EquipmentSlot.MAINHAND));
        playerEntity.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
    }

    public boolean hasTwoHandhelds(Player player) {
        return hasTwoHandhelds(player.getItemBySlot(EquipmentSlot.MAINHAND), player.getItemBySlot(EquipmentSlot.OFFHAND));
    }


    public boolean hasTwoHandhelds(ItemStack mainHand, ItemStack offHand) {
        return
                hasValue(mainHand) &&
                hasValue(offHand);
    }
}
