package smartin.miapi.compat.kubejs;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.registries.RegistryInventory;

public class ModularWeaponBuilder extends ItemBuilder {
    public ModularWeaponBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public Item createObject() {
        return new ModularWeapon(createItemProperties(), true) {{
            RegistryInventory.modularItems.registerWithoutRegistrar(id, this);
        }};
    }

    public static class Arrow extends ItemBuilder {
        public Arrow(ResourceLocation i) {
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
        public Axe(ResourceLocation i) {
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
        public Boots(ResourceLocation i) {
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
        public Chestplate(ResourceLocation i) {
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
        public Crossbow(ResourceLocation i) {
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
        public Helmet(ResourceLocation i) {
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
        public Hoe(ResourceLocation i) {
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
        public Leggings(ResourceLocation i) {
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
        public Pickaxe(ResourceLocation i) {
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
        public Shovel(ResourceLocation i) {
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
        public Sword(ResourceLocation i) {
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
