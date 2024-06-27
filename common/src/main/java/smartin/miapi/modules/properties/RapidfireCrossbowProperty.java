package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.items.ModularCrossbow;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class RapidfireCrossbowProperty extends DoubleProperty {
    public static String KEY = "rapid_fire_crossbow";
    public static String NBTKEY = Miapi.MOD_ID + KEY;
    public static RapidfireCrossbowProperty property;

    public RapidfireCrossbowProperty() {
        super(KEY);
        property = this;
        MiapiProjectileEvents.MODULAR_CROSSBOW_LOAD_AFTER.register(context -> {
            int shotCount = getShotCount(context.crossbow);
            List<ItemStack> projectiles = new ArrayList<>(getSavedProjectilesOnCrossbow(context.crossbow));
            for (int i = projectiles.size(); i < shotCount; i++) {
                ItemStack otherAmmo = context.player.getProjectile(context.crossbow);
                Player player = null;
                if (
                        !otherAmmo.isEmpty() &&
                        context.player instanceof Player player2 &&
                        !player2.isCreative()) {
                    player = player2;
                } else {
                    otherAmmo = otherAmmo.copy();
                }
                otherAmmo = otherAmmo.split(1);
                if (!otherAmmo.isEmpty()) {
                    if (otherAmmo.isDamageableItem()) {
                        otherAmmo.hurtAndBreak(1, context.player, livingEntity -> {
                        });
                    }
                    projectiles.add(otherAmmo.copy());
                    if (player != null) {
                        player.getInventory().removeItem(otherAmmo);
                    }
                }
            }
            writeProjectileListToCrossbow(context.crossbow, projectiles);
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.register((player, crossbow) -> {
            if (crossbow.getItem() instanceof ModularCrossbow) {
                List<ItemStack> projectiles = getSavedProjectilesOnCrossbow(crossbow);
                if (!projectiles.isEmpty()) {
                    ItemStack itemStack = projectiles.remove(0);
                    ModularCrossbow.putProjectile(crossbow, itemStack);
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow) > 0) {
                        ModularCrossbow.putProjectile(crossbow, itemStack.copy());
                        ModularCrossbow.putProjectile(crossbow, itemStack.copy());
                    }
                    writeProjectileListToCrossbow(crossbow, projectiles);
                    ModularCrossbow.setCharged(crossbow, true);
                    if(player instanceof ServerPlayer serverPlayerEntity){
                        serverPlayerEntity.getCooldowns().addCooldown(
                                crossbow.getItem(),
                                (int) MagazineCrossbowShotDelay.property.getValueSafe(crossbow));
                    }
                }
            }
            return EventResult.pass();
        });
        LoreProperty.loreSuppliers.add((stack, world, tooltip, context) -> {
            if (stack.getItem() instanceof ModularItem) {
                List<ItemStack> projectiles = getSavedProjectilesOnCrossbow(stack);
                for (ItemStack projectile : projectiles) {
                    tooltip.add(Component.translatable(Miapi.MOD_ID + ".crossbow.ammo.addition", projectile.getHoverName()));
                }
            }
        });
    }

    public static void writeProjectileListToCrossbow(ItemStack crossbow, List<ItemStack> projectiles) {
        ListTag list = new ListTag();
        for (ItemStack itemStack : projectiles) {
            list.add(itemStack.writeNbt(new CompoundTag()));
        }
        crossbow.getOrCreateNbt().put(NBTKEY, list);
    }

    public static List<ItemStack> getSavedProjectilesOnCrossbow(ItemStack crossbow) {
        List<ItemStack> projectiles = new ArrayList<>();
        if (crossbow.hasNbt()) {
            if (crossbow.getOrCreateNbt().get(NBTKEY) instanceof ListTag list) {
                for (int i = 0; i < list.size(); i++) {
                    projectiles.add(ItemStack.parse((CompoundTag) list.get(i)));
                }
            }
        }
        return projectiles;
    }

    public static int getShotCount(ItemStack itemStack) {
        return (int) property.getValueSafeRaw(itemStack);
    }

    @Override
    public Double getValue(ItemStack stack) {
        Double value = getValueRaw(stack);
        if (value != null) {
            return (double) value.intValue() + 1;
        }
        return null;
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return (int) getValueSafeRaw(stack) + 1;
    }
}
