package smartin.miapi.compat.kubejs;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.registries.RegistryInventory;

public class ModularWeaponBuilder extends ItemBuilder {
    public ModularWeaponBuilder(Identifier i) {
        super(i);
    }

    @Override
    public Item createObject() {
        return new ModularWeapon(createItemProperties(), true) {{
            RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
        }};
    }

    public static class Arrow extends ItemBuilder {
        public Arrow(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularArrow(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Axe extends ItemBuilder {
        public Axe(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularAxe(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Boots extends ItemBuilder {
        public Boots(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularBoots(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Chestplate extends ItemBuilder {
        public Chestplate(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularChestPlate(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Crossbow extends ItemBuilder {
        public Crossbow(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularCrossbow(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Helmet extends ItemBuilder {
        public Helmet(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularHelmet(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Hoe extends ItemBuilder {
        public Hoe(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularHoe(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Leggings extends ItemBuilder {
        public Leggings(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularLeggings(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Pickaxe extends ItemBuilder {
        public Pickaxe(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularPickaxe(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Shovel extends ItemBuilder {
        public Shovel(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularShovel(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }

    public static class Sword extends ItemBuilder {
        public Sword(Identifier i) {
            super(i);
        }

        @Override
        public Item createObject() {
            return new ModularSword(createItemProperties()) {{
                RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
            }};
        }
    }
}
