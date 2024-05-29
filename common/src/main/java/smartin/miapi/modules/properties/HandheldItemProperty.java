package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.BooleanProperty;

import java.util.UUID;

public class HandheldItemProperty extends BooleanProperty {
    public static UUID attackspeedUUID = UUID.fromString("134d982f-c8ab-4b04-969f-15b495f89abd");

    public static String KEY = "handheld_item";

    public HandheldItemProperty() {
        super(KEY, false);
        MiapiEvents.PLAYER_EQUIP_EVENT.register((player, changes) -> {
            if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                ItemStack mainHandItem = changes.getOrDefault(EquipmentSlot.MAINHAND, player.getEquippedStack(EquipmentSlot.MAINHAND));
                ItemStack offHandItem = changes.getOrDefault(EquipmentSlot.OFFHAND, player.getEquippedStack(EquipmentSlot.OFFHAND));
                boolean hasAttribute = serverPlayerEntity
                                               .getAttributes()
                                               .getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED).getModifier(attackspeedUUID) != null;
                if (hasTwoHandhelds(mainHandItem, offHandItem)) {
                    if(!hasAttribute){
                        serverPlayerEntity
                                .getAttributes()
                                .getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                                .addTemporaryModifier(
                                        new EntityAttributeModifier(
                                                attackspeedUUID,
                                                "temphandheldboni",
                                                1.5,
                                                EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    }

                } else {
                    if(hasAttribute){
                        serverPlayerEntity
                                .getAttributes()
                                .getCustomInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                                .removeModifier(attackspeedUUID);
                    }
                }
            }
            return EventResult.pass();
        });

        MiapiEvents.LIVING_HURT_AFTER.register(event -> {
            if (event.damageSource != null && event.damageSource.getAttacker() instanceof PlayerEntity serverPlayerEntity) {
                if (hasTwoHandhelds(serverPlayerEntity) && !Platform.isModLoaded("bettercombat")) {
                    Miapi.LOGGER.info("swap");
                    swapHands(serverPlayerEntity);
                }
            }
            return EventResult.pass();
        });
        LoreProperty.loreSuppliers.add((stack, world, tooltip, context) -> {
            if (hasValue(stack)) {
                tooltip.add(Text.translatable(Miapi.MOD_ID + ".handheld.tooltip"));
            }
        });
    }

    public void swapHands(PlayerEntity playerEntity) {
        ItemStack itemStack = playerEntity.getEquippedStack(EquipmentSlot.OFFHAND);
        playerEntity.equipStack(EquipmentSlot.OFFHAND, playerEntity.getEquippedStack(EquipmentSlot.MAINHAND));
        playerEntity.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    public boolean hasTwoHandhelds(PlayerEntity player) {
        return hasTwoHandhelds(player.getEquippedStack(EquipmentSlot.MAINHAND), player.getEquippedStack(EquipmentSlot.OFFHAND));
    }


    public boolean hasTwoHandhelds(ItemStack mainHand, ItemStack offHand) {
        return
                hasValue(mainHand) &&
                hasValue(offHand);
    }
}
