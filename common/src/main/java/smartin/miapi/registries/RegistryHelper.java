package smartin.miapi.registries;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class RegistryHelper {
    public static final Map<Level, RegistryAccess> registryLookup = new WeakHashMap<>();

    public static void setup() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(world -> registryLookup.put(world, world.registryAccess()));
        if (Platform.getEnv() == EnvType.CLIENT) {
            setupClient();
        }
    }

    @Environment(EnvType.CLIENT)
    public static void setupClient() {
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(world -> registryLookup.put(world, world.registryAccess()));
    }

    public static RegistryAccess tryFind(RegistryOps.RegistryInfoLookup lookup) {
        return registryLookup.values().stream()
                .filter(a -> {
                    Optional<HolderLookup.RegistryLookup<Enchantment>> enchantmentInfoA = a.lookup(Registries.ENCHANTMENT);
                    Optional<RegistryOps.RegistryInfo<Enchantment>> enchantmentInfoLookup = lookup.lookup(Registries.ENCHANTMENT);

                    return enchantmentInfoA.isPresent() && enchantmentInfoLookup
                            .map(info -> enchantmentInfoA.get().canSerializeIn(info.owner()))
                            .orElse(false);
                })
                .findFirst()
                .orElse(Miapi.registryAccess);
    }

}
