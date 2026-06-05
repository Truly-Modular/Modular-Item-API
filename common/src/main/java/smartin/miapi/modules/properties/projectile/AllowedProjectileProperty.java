package smartin.miapi.modules.properties.projectile;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AllowedProjectileProperty extends CodecProperty<List<TagKey<Item>>> {
    public static final AllowedProjectileProperty PROPERTY = new AllowedProjectileProperty();
    public static final ResourceLocation KEY = Miapi.id("crossbow_allowed_projectile_overwrite");

    protected AllowedProjectileProperty() {
        super(Miapi.toListOrSimple(TagKey.codec(Registries.ITEM)));
    }

    public static Predicate<ItemStack> getAllowedProjectiles(ProjectileWeaponItem item, ItemStack weapon, Supplier<Predicate<ItemStack>> fallback) {
        if (ModularItem.isModularItem(weapon)) {
            return PROPERTY.getData(weapon).map(AllowedProjectileProperty::fromList).orElseGet(fallback);
        }
        return fallback.get();
    }

    public static Predicate<ItemStack> getAllowedHandProjectiles(ProjectileWeaponItem item, ItemStack weapon, Supplier<Predicate<ItemStack>> fallback) {
        if (ModularItem.isModularItem(weapon)) {
            return PROPERTY.getData(weapon).map(AllowedProjectileProperty::fromList).orElseGet(fallback);
        }
        return fallback.get();
    }

    private static Predicate<ItemStack> fromList(List<TagKey<Item>> list) {
        return (itemStack -> {
            for (TagKey<Item> tag : list) {
                if (itemStack.is(tag)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public List<TagKey<Item>> merge(List<TagKey<Item>> left, List<TagKey<Item>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}