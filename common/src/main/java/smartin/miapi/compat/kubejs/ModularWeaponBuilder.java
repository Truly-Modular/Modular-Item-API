package smartin.miapi.compat.kubejs;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.item.modular.items.armor.ModularBoots;
import smartin.miapi.item.modular.items.armor.ModularChestPlate;
import smartin.miapi.item.modular.items.armor.ModularHelmet;
import smartin.miapi.item.modular.items.armor.ModularLeggings;
import smartin.miapi.item.modular.items.bows.ModularArrow;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.item.modular.items.tools.*;
import smartin.miapi.registries.RegistryInventory;

public class ModularWeaponBuilder extends ItemBuilder {
    public ModularWeaponBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public Item createObject() {
        return new ModularWeapon(createItemProperties(), true) {{
            RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
        }};
    }

    public static class Arrow extends ItemBuilder {
        public Arrow(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularArrow(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Axe extends ItemBuilder {
        public Axe(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularAxe(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Boots extends ItemBuilder {
        public Boots(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularBoots(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Chestplate extends ItemBuilder {
        public Chestplate(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularChestPlate(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Crossbow extends ItemBuilder {
        public Crossbow(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularCrossbow(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Helmet extends ItemBuilder {
        public Helmet(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularHelmet(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Hoe extends ItemBuilder {
        public Hoe(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularHoe(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Leggings extends ItemBuilder {
        public Leggings(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularLeggings(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Pickaxe extends ItemBuilder {
        public Pickaxe(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularPickaxe(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Shovel extends ItemBuilder {
        public Shovel(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularShovel(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Sword extends ItemBuilder {
        public Sword(ResourceLocation i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularSword(createItemProperties()) {{
                RegistryInventory.MODULAR_ITEMS.registerWithoutRegistrar(id, this);
            }};
        }
    }
}
