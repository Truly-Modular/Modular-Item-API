package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.items.ModularCrossbow;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.List;

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
                ItemStack otherAmmo = context.player.getProjectileType(context.crossbow);
                PlayerEntity player = null;
                if (
                        !otherAmmo.isEmpty() &&
                        context.player instanceof PlayerEntity player2 &&
                        !player2.isCreative()) {
                    player = player2;
                } else {
                    otherAmmo = otherAmmo.copy();
                }
                otherAmmo = otherAmmo.split(1);
                if (!otherAmmo.isEmpty()) {
                    if (otherAmmo.isDamageable()) {
                        otherAmmo.damage(1, context.player, livingEntity -> {
                        });
                    }
                    projectiles.add(otherAmmo.copy());
                    if (player != null) {
                        player.getInventory().removeOne(otherAmmo);
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
                    if (EnchantmentHelper.getLevel(Enchantments.MULTISHOT, crossbow) > 0) {
                        ModularCrossbow.putProjectile(crossbow, itemStack.copy());
                        ModularCrossbow.putProjectile(crossbow, itemStack.copy());
                    }
                    writeProjectileListToCrossbow(crossbow, projectiles);
                    ModularCrossbow.setCharged(crossbow, true);
                    if(player instanceof ServerPlayerEntity serverPlayerEntity){
                        serverPlayerEntity.getItemCooldownManager().set(
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
                    tooltip.add(Text.translatable(Miapi.MOD_ID + ".crossbow.ammo.addition", projectile.getName()));
                }
            }
        });
    }

    public static void writeProjectileListToCrossbow(ItemStack crossbow, List<ItemStack> projectiles) {
        NbtList list = new NbtList();
        for (ItemStack itemStack : projectiles) {
            list.add(itemStack.writeNbt(new NbtCompound()));
        }
        Miapi.LOGGER.info(list.toString());
        crossbow.getOrCreateNbt().put(NBTKEY, list);
    }

    public static List<ItemStack> getSavedProjectilesOnCrossbow(ItemStack crossbow) {
        List<ItemStack> projectiles = new ArrayList<>();
        if (crossbow.hasNbt()) {
            if (crossbow.getOrCreateNbt().get(NBTKEY) instanceof NbtList list) {
                for (int i = 0; i < list.size(); i++) {
                    projectiles.add(ItemStack.fromNbt((NbtCompound) list.get(i)));
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
