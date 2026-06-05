package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.screen.preview.InventoryPreviewManager;
import smartin.miapi.modules.properties.projectile.AllowedProjectileProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.function.Predicate;

public class IsAmmoFeatureType implements InventoryFeatureType<Boolean> {
    public static final IsAmmoFeatureType FEATURE = new IsAmmoFeatureType();
    public static final FacetKey<SimpleEntityFacet<Boolean>> FACET = SimpleEntityFacet
            .createSimple(Miapi.id("ammo_before_inv"), Codec.BOOL)
            .setPredicate(e -> true)
            .syncToClientsOnSet(true)
            .setSaveCondition(b -> !b)
            .build(true);

    private IsAmmoFeatureType() {
    }

    public static final ResourceLocation ID =
            Miapi.id("is_ammo");

    @Override
    public Boolean merge(Boolean left, Boolean right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    public static ItemStack findStackBeforeVanilla(Player player, ItemStack weapon) {
        if (weapon.getItem() instanceof ProjectileWeaponItem projectileWeaponItem && FACET.getOptional(player).map(SimpleEntityFacet::get).orElse(true)) {
            return findAmmo(player, weapon, projectileWeaponItem);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findStackAfterVanilla(Player player, ItemStack weapon) {
        if (weapon.getItem() instanceof ProjectileWeaponItem projectileWeaponItem && !FACET.getOptional(player).map(SimpleEntityFacet::get).orElse(true)) {
            return findAmmo(player, weapon, projectileWeaponItem);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findAmmo(
            Player player,
            ItemStack weapon,
            ProjectileWeaponItem projectileWeaponItem
    ) {
        if (weapon.isEmpty() || player == null) {
            return ItemStack.EMPTY;
        }
        Predicate<ItemStack> predicate = AllowedProjectileProperty.getAllowedProjectiles(projectileWeaponItem, weapon, () -> ModularCrossbow.getAllSupportedProjectilesWithApi(projectileWeaponItem, weapon));
        for (InventoryInstance instance :
                ItemInventoryManager.getInventoriesWith(
                        player,
                        IsAmmoFeatureType.FEATURE,
                        b -> b
                ).toList()) {
            Container container = instance.create();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                if (predicate.test(stack)) {
                    if(player instanceof ServerPlayer sp){
                        InventoryPreviewManager.PREVIEW.sendToClientPlayer(sp, instance.getType().getId());
                    }
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }

    @Override
    public Boolean initialize(Boolean property, ModuleInstance context) {
        return property;
    }
}