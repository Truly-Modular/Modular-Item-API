package smartin.miapi.modules;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import smartin.miapi.Miapi;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MiapiHolderLookupAdapter implements RegistryOps.RegistryInfoLookup {
    private final HolderLookup.Provider lookupProvider;
    Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new ConcurrentHashMap<>();

    public MiapiHolderLookupAdapter(HolderLookup.Provider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return (Optional) this.lookups.computeIfAbsent(registryKey, this::createLookup);
    }

    private Optional<RegistryOps.RegistryInfo<Object>> createLookup(ResourceKey<? extends Registry<?>> registryKey) {
        if (this.lookupProvider == null) {
            if (Miapi.registryAccess != null) {
                return Miapi.registryAccess.lookup(registryKey).map(RegistryOps.RegistryInfo::fromRegistryLookup);
            }
            if (Miapi.clientRegistryAccess != null) {
                return Miapi.clientRegistryAccess.lookup(registryKey).map(RegistryOps.RegistryInfo::fromRegistryLookup);
            }
            return Optional.empty();
        }
        return this.lookupProvider.lookup(registryKey).map(RegistryOps.RegistryInfo::fromRegistryLookup);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            if (object instanceof MiapiHolderLookupAdapter) {
                MiapiHolderLookupAdapter registryops$holderlookupadapter = (MiapiHolderLookupAdapter) object;
                if (this.lookupProvider.equals(registryops$holderlookupadapter.lookupProvider)) {
                    return true;
                }
            }

            return false;
        }
    }

    public int hashCode() {
        return this.lookupProvider.hashCode();
    }
}