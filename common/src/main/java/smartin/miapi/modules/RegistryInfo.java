package smartin.miapi.modules;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;

public record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
        public RegistryInfo(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
            this.owner = owner;
            this.getter = getter;
            this.elementsLifecycle = elementsLifecycle;
        }

        public static <T> RegistryInfo<T> fromRegistryLookup(HolderLookup.RegistryLookup<T> registryLookup) {
            return new RegistryInfo<>(registryLookup, registryLookup, registryLookup.registryLifecycle());
        }

        public HolderOwner<T> owner() {
            return this.owner;
        }

        public HolderGetter<T> getter() {
            return this.getter;
        }

        public Lifecycle elementsLifecycle() {
            return this.elementsLifecycle;
        }
    }