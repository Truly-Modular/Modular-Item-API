package smartin.miapi.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class KubeJSMiapiPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("miapi_modular_weapon", ModularWeaponBuilder.class, ModularWeaponBuilder::new);
        RegistryInfo.ITEM.addType("miapi_modular_arrow", ModularWeaponBuilder.Arrow.class, ModularWeaponBuilder.Arrow::new);
        RegistryInfo.ITEM.addType("miapi_modular_axe", ModularWeaponBuilder.Axe.class, ModularWeaponBuilder.Axe::new);
        RegistryInfo.ITEM.addType("miapi_modular_boots", ModularWeaponBuilder.Boots.class, ModularWeaponBuilder.Boots::new);
        RegistryInfo.ITEM.addType("miapi_modular_chestplate", ModularWeaponBuilder.Chestplate.class, ModularWeaponBuilder.Chestplate::new);
        RegistryInfo.ITEM.addType("miapi_modular_crossbow", ModularWeaponBuilder.Crossbow.class, ModularWeaponBuilder.Crossbow::new);
        RegistryInfo.ITEM.addType("miapi_modular_helmet", ModularWeaponBuilder.Helmet.class, ModularWeaponBuilder.Helmet::new);
        RegistryInfo.ITEM.addType("miapi_modular_hoe", ModularWeaponBuilder.Hoe.class, ModularWeaponBuilder.Hoe::new);
        RegistryInfo.ITEM.addType("miapi_modular_leggings", ModularWeaponBuilder.Leggings.class, ModularWeaponBuilder.Leggings::new);
        RegistryInfo.ITEM.addType("miapi_modular_pickaxe", ModularWeaponBuilder.Pickaxe.class, ModularWeaponBuilder.Pickaxe::new);
        RegistryInfo.ITEM.addType("miapi_modular_shovel", ModularWeaponBuilder.Shovel.class, ModularWeaponBuilder.Shovel::new);
        RegistryInfo.ITEM.addType("miapi_modular_sword", ModularWeaponBuilder.Sword.class, ModularWeaponBuilder.Sword::new);
    }
}
